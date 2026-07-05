package com.euonia.bus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.annotation.Channel;
import com.euonia.bus.contract.Message;
import com.euonia.bus.contract.Request;
import com.euonia.bus.exception.MessageConventionException;
import com.euonia.bus.exception.MessageTransportException;
import com.euonia.bus.exception.MessageTypeException;
import com.euonia.bus.message.PipelineMessage;
import com.euonia.bus.options.CallOptions;
import com.euonia.bus.options.ExtendableOptions;
import com.euonia.bus.options.PublishOptions;
import com.euonia.bus.options.SendOptions;
import com.euonia.core.GuidType;
import com.euonia.core.ObjectId;
import com.euonia.http.RequestContextAccessor;
import com.euonia.pipeline.PipelineFactory;
import com.euonia.reflection.ServiceProvider;
import com.euonia.utility.StringUtility;

/**
 * 消息总线的默认实现，实现 {@link Bus} 接口。
 * <p>
 * {@code MessageBus} 是 Euonia 消息系统的核心组件，负责协调消息的发布、发送和请求-响应调用。
 * 它利用 {@link Dispatcher} 确定消息应路由到哪些传输实例，
 * 并支持可选的管道行为（{@code Pipeline}）来处理消息拦截、日志记录、验证等横切关注点。
 * <p>
 * 支持三种消息传递模式：
 * <ul>
 *   <li><b>发布/订阅</b> — 通过 {@link #publishAsync} 将多播消息发送到所有匹配的传输实例</li>
 *   <li><b>发送/命令</b> — 通过 {@link #sendAsync} 将单播消息发送到单个处理程序并等待响应</li>
 *   <li><b>请求/响应</b> — 通过 {@link #callAsync} 发送请求并期待类型化的响应</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class MessageBus implements Bus {

    /**
     * 日志记录器。
     */
    private static final Logger LOGGER = Logger.getLogger(MessageBus.class.getName());

    /**
     * 消息分发器，用于确定消息应路由到哪些传输实例。
     */
    private final Dispatcher dispatcher;

    /**
     * 服务提供者，用于获取传输和其他依赖服务。
     */
    private final ServiceProvider provider;

    /**
     * 配置器，用于获取消息约定和传输策略。
     */
    private final Configurator configurator;

    /**
     * 管道工厂，用于创建消息处理管道。
     */
    private final PipelineFactory pipelineFactory;

    /**
     * 使用服务提供者创建消息总线实例。
     * <p>
     * 从服务提供者中解析 {@link Dispatcher}、{@link Configurator}、 {@link PipelineFactory}。
     *
     * @param provider 服务提供者，必须包含 Dispatcher 服务
     * @throws IllegalStateException 如果 Dispatcher 服务未找到
     */
    public MessageBus(ServiceProvider provider) {
        this.provider = provider;
        this.dispatcher = provider.getService(Dispatcher.class).orElseThrow(() -> new IllegalStateException("Dispatcher service not found"));
        this.configurator = provider.getService(Configurator.class).orElseThrow(() -> new IllegalStateException("Configurator service not found"));
        this.pipelineFactory = provider.getService(PipelineFactory.class).orElse(null);
    }

    /**
     * 使用指定的参数创建消息总线实例。
     *
     * @param provider     服务提供者
     * @param configurator 消息总线配置选项
     */
    public MessageBus(ServiceProvider provider, Configurator configurator) {
        this.provider = provider;
        this.configurator = configurator;
        this.dispatcher = provider.getService(Dispatcher.class).orElseGet(() -> new StrategicDispatcher(configurator));
        this.pipelineFactory = provider.getService(PipelineFactory.class).orElse(null);
    }

    /**
     * 以发布/订阅模式异步发布一条多播消息。
     * <p>
     * 消息将被发送到 {@link Dispatcher} 确定的所有匹配传输实例。
     * 如果启用了管道行为，消息会先经过管道处理再发送。
     *
     * @param <T>      消息负载类型
     * @param message  要发布的消息
     * @param options  发布选项，可以为 {@code null}
     * @param behavior 可选的管道行为配置回调
     * @return 在所有传输实例完成发送后完成的 future
     * @throws MessageTypeException 如果消息类型不是多播类型
     */
    @Override
    public <T> CompletableFuture<Void> publishAsync(T message, PublishOptions options, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
        if (options == null) {
            options = new PublishOptions();
        }

        var messageType = message.getClass();

        var channelName = getChannelName(messageType, options);

        if (!configurator.getConvention().isMulticast(channelName)) {
            throw new MessageConventionException("The message type " + message.getClass().getName() + " is not multicast");
        }

        var context = RequestContextAccessor.get();
        RoutedMessage<T> pack = new RoutedMessage<>(message, channelName, options.getMessageId());
        pack.setRequestTrackId(StringUtility.collapse(context::getTraceIdentifier, context::getRequestId, () -> ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString()));
        pack.setAuthorization(context.getAuthorization());

        if (options.getMetadataSetter() != null) {
            options.getMetadataSetter().accept(pack.getMetadata());
        }

        return executePipelineAsync(pack, messageType, behavior, options)
            .thenCompose(finalPack -> {
                RequestContextAccessor.set(context);
                try {
                    var transports = dispatcher.determine(channelName, messageType);

                    var tasks = transports.stream()
                                          .map(transport -> {
                                              LOGGER.info(() -> String.format("Message '%s'(%s) transport via %s on channel: %s", finalPack.getMessageId(), messageType.getName(), transport, channelName));
                                              return provider.getService(Transport.class, transport)
                                                             .orElseThrow(() -> new MessageTransportException("Transport service not found: " + transport))
                                                             .publishAsync(finalPack);
                                          })
                                          .toArray(CompletableFuture[]::new);

                    return CompletableFuture.allOf(tasks);
                } finally {
                    RequestContextAccessor.remove();
                }
            });
    }

    /**
     * 以发送/命令模式异步发送一条单播消息并等待响应。
     * <p>
     * 消息将被发送到 {@link Dispatcher} 确定的第一个传输实例。
     * 如果启用了管道行为，消息会先经过管道处理再发送。
     *
     * @param <T>          消息负载类型
     * @param <R>          响应类型
     * @param message      要发送的消息
     * @param responseType 期望的响应类型
     * @param callback     可选的回调，用于接收响应或错误
     * @param options      发送选项，可以为 {@code null}
     * @param behavior     可选的管道行为配置回调
     * @return 在消息处理完毕时完成的 future
     * @throws MessageTypeException 如果消息类型不是单播类型
     */
    @Override
    public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback, SendOptions options, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
        if (options == null) {
            options = new SendOptions();
        }

        var messageType = message.getClass();

        var channelName = getChannelName(messageType, options);

        if (!configurator.getConvention().isUnicast(channelName)) {
            throw new MessageConventionException("The message type " + message.getClass().getName() + " is not unicast");
        }

        var context = RequestContextAccessor.get();

        RoutedMessage<T> pack = new RoutedMessage<>(message, channelName, options.getMessageId());
        pack.setRequestTrackId(StringUtility.collapse(context::getTraceIdentifier, context::getRequestId, () -> ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString()));
        pack.setAuthorization(context.getAuthorization());
        pack.setCorrelationId(StringUtility.collapse(options::getCorrelationId, () -> ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString()));

        if (options.getMetadataSetter() != null) {
            options.getMetadataSetter().accept(pack.getMetadata());
        }

        return executePipelineAsync(pack, messageType, behavior, options)
            .thenCompose(finalPack -> {
                RequestContextAccessor.set(context);
                try {
                    var transports = dispatcher.determine(channelName, messageType);

                    if (transports.isEmpty()) {
                        throw new MessageTransportException(String.format("No transport found for message type: %s", messageType.getName()));
                    } else if (transports.size() > 1) {
                        throw new MessageTypeException("Multiple transport found for message type: " + messageType.getName());
                    }

                    var transportName = transports.get(0);

                    var transport = provider.getService(Transport.class, transportName)
                                            .orElseThrow(() -> new MessageTransportException("The transport " + transportName + " was not found."));

                    LOGGER.info(() -> String.format("Message '%s'(%s) transport via %s on channel: %s", finalPack.getMessageId(), messageType.getName(), transport, channelName));

                    return transport.sendAsync(finalPack, responseType)
                                    .whenComplete((response, ex) -> {
                                        if (ex != null) {
                                            LOGGER.log(Level.SEVERE, ex, () -> "Failed to send message: " + messageType.getName());
                                            if (callback != null) {
                                                callback.onError(ex);
                                            } else {
                                                if (ex instanceof RuntimeException exception) {
                                                    throw exception;
                                                } else {
                                                    throw new RuntimeException(ex);
                                                }
                                            }
                                        } else {
                                            if (callback != null) {
                                                callback.onNext(response);
                                            }
                                        }
                                    })
                                    .thenAccept(v -> {
                                        if (callback != null) {
                                            callback.onComplete();
                                        }
                                    });
                } finally {
                    RequestContextAccessor.remove();
                }
            });
    }

    /**
     * 以请求/响应模式异步发送一条请求消息并期待类型化的响应。
     * <p>
     * 消息将被发送到 {@link Dispatcher} 确定的第一个传输实例。
     * 如果启用了管道行为，消息会先经过管道处理再发送。
     *
     * @param <T>          请求类型，必须实现 {@link Request}
     * @param <R>          响应类型
     * @param request      请求消息
     * @param responseType 期望的响应类型
     * @param options      调用选项，可以为 {@code null}
     * @param behavior     可选的管道行为配置回调
     * @return 在收到响应时完成并携带响应结果的 future
     * @throws MessageTypeException 如果消息类型不是请求类型
     */
    @Override
    public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, CallOptions options, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
        if (options == null) {
            options = new CallOptions();
        }

        var messageType = request.getClass();

        var channelName = getChannelName(messageType, options);

        if (!configurator.getConvention().isRequest(channelName)) {
            throw new MessageConventionException("The message type " + messageType.getName() + " is not request");
        }

        var context = RequestContextAccessor.get();

        RoutedMessage<T> pack = new RoutedMessage<>(request, channelName, options.getMessageId());
        pack.setRequestTrackId(StringUtility.collapse(context::getTraceIdentifier, context::getRequestId, () -> ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString()));
        pack.setAuthorization(context.getAuthorization());
        pack.setCorrelationId(StringUtility.collapse(options::getCorrelationId, () -> ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString()));

        if (options.getMetadataSetter() != null) {
            options.getMetadataSetter().accept(pack.getMetadata());
        }

        return executePipelineAsync(pack, messageType, behavior, options)
            .thenCompose(finalPack -> {
                RequestContextAccessor.set(context);
                try {
                    var transportNames = dispatcher.determine(channelName, messageType);
                    if (transportNames.isEmpty()) {
                        throw new MessageTransportException(String.format("No transport found for message type: %s", messageType.getName()));
                    }

                    var transportName = transportNames.get(0);

                    var transport = provider.getService(Transport.class, transportName)
                                            .orElseThrow(() -> new MessageTransportException("The transport " + transportName + " was not found."));

                    LOGGER.info(() -> String.format("Message '%s'(%s) transport via %s on channel: %s", finalPack.getMessageId(), messageType.getName(), transportName, channelName));

                    return transport.callAsync(finalPack, responseType);
                } finally {
                    RequestContextAccessor.remove();
                }
            });
    }

    private <T, R> CompletableFuture<RoutedMessage<T>> executePipelineAsync(RoutedMessage<T> pack, Class<?> messageType, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior, ExtendableOptions options) {

        // 优先使用 options 中的配置，如果未设置，则使用 configurator 的全局配置
        var isEnablePipelineBehaviors = options.isEnablePipelineBehaviors() != null ? (boolean)options.isEnablePipelineBehaviors() : configurator.isEnablePipelineBehaviors();

        if (!isEnablePipelineBehaviors) {
            return CompletableFuture.completedFuture(pack);
        }

        var pipeline = pipelineFactory.<RoutedMessage<T>, R>create();
        if (pipeline == null) {
            return CompletableFuture.completedFuture(pack);
        }

        var future = new CompletableFuture<RoutedMessage<T>>();
        var pipelineMessage = new PipelineMessage<>(pack, pipeline);
        if (options.isAttachDefaultPipelineBehaviors()) {
            pipelineMessage.getPipeline().useOf(messageType, true);
        }

        if (behavior != null) {
            behavior.accept(pipelineMessage);
        }
        pipelineMessage.executeAsync()
                       .whenComplete((result, error) -> {
                           if (error != null) {
                               LOGGER.log(Level.SEVERE, error, () -> String.format("Pipeline execution failed for message: %s", messageType.getName()));
                               future.completeExceptionally(error);
                           } else {
                               future.complete(pipelineMessage.getMessage());
                           }
                       });
        return future;
    }

    private String getChannelName(Class<?> messageType, ExtendableOptions options) {

        String channelName;

        if (!StringUtility.isNullOrBlank(options.getChannel())) {
            channelName = options.getChannel();
        } else if (messageType.getAnnotation(Channel.class) != null) {
            channelName = messageType.getAnnotation(Channel.class).value();
        } else if (Message.class.isAssignableFrom(messageType)) {
            channelName = messageType.getName();
        } else {
            throw new MessageTypeException(String.format("The message type '%s' is not registered and does not implement the Message interface. Please specify a channel name in the options.", messageType.getName()));
        }
        return channelName;
    }
}
