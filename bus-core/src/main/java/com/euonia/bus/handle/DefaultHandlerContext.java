package com.euonia.bus.handle;

import java.lang.reflect.InvocationTargetException;

import com.euonia.bus.*;
import com.euonia.bus.contract.Message;
import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.convention.BaseMessageConvention;
import com.euonia.bus.event.MessageSubscribedEvent;
import com.euonia.bus.inbox.InboxStore;
import com.euonia.reflection.ServiceProvider;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 默认的消息处理器上下文，使用 Euonia 的 {@link ServiceProvider} 进行依赖解析。
 * <p>
 * 该类管理处理器注册并将传入的消息分派到适当的处理器。
 * 它支持泛型处理器类型注册（通过 DI 解析）和基于反射的注册（通过 {@link ChannelRegistration}） 。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class DefaultHandlerContext implements HandlerContext {

    private static final Logger LOGGER = Logger.getLogger(DefaultHandlerContext.class.getName());

    private final ConcurrentMap<String, List<MessageHandlerContext>> handlerContainer = new ConcurrentHashMap<>();
    private final SubmissionPublisher<MessageSubscribedEvent> publisher = new SubmissionPublisher<>();
    private final MessageConvention convention;
    private final InboxStore inboxStore;
    private final IdempotentHandler idempotentHandler;

    /**
     * 初始化 {@link DefaultHandlerContext} 的新实例。
     *
     * @param provider 用于解析处理器和其他服务的服务提供者
     */
    public DefaultHandlerContext(ServiceProvider provider) {
        this.convention = provider.getService(MessageConvention.class)
                                  .orElse(new BaseMessageConvention());
        this.inboxStore = provider.getService(InboxStore.class)
                                  .orElse(null);
        this.idempotentHandler = new IdempotentHandler(provider, handlerContainer);

        this.idempotentHandler.start();
    }

    // region 事件订阅

    @Override
    public void onMessageSubscribed(Consumer<MessageSubscribedEvent> listener) {
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(MessageSubscribedEvent event) {
                listener.accept(event);
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.log(Level.SEVERE, "Error occurred in message subscription event", throwable);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    // endregion

    // region 处理器注册

    /**
     * 为给定的消息类型注册一个消息处理器类型。
     * 处理器将在分发时从 {@link ServiceProvider} 中解析。
     *
     * @param <M>         消息类型
     * @param <R>         响应类型
     * @param <H>         实现 {@link Handler}{@code <M, R>} 的处理器类型
     * @param channel     要注册处理器的通道名称
     * @param messageType 消息的类
     * @param handlerType 处理器的类
     */
    public <M extends Message, R, H extends Handler<M, R>> void register(String channel, Class<M> messageType, Class<H> handlerType) {
        @SuppressWarnings("unchecked")
        MessageHandlerFactory factory = sp -> {
            var handler = sp.getRequiredService(handlerType);
            return (message, context) -> {
                @SuppressWarnings("unchecked")
                var result = handler.handle((M) message, context);
                return result;
            };
        };

        var factoryContext = new MessageHandlerContext(handlerType, factory);

        concurrentDictionarySafeRegister(channel, factoryContext);
        publisher.submit(new MessageSubscribedEvent(channel, messageType, handlerType));
    }

    /**
     * 注册由 {@link ChannelHandler} 描述的处理器。
     * 该注册包含处理器类型、要调用的方法和通道名称。
     *
     * @param channel        要注册处理器的通道名称
     * @param channelHandler 描述要注册的处理器的 {@link ChannelHandler}
     */
    public void register(String channel, ChannelHandler channelHandler) {
        // 一次性设置 accessible，避免每次消息处理时的重复安全检查
        var method = channelHandler.method();
        method.setAccessible(true);

        MessageHandlerFactory factory = sp -> {
            var handler = channelHandler.instance() == null ? sp.getServiceOrCreate(channelHandler.handlerType()) : channelHandler.instance();
            return (message, context) -> {
                var args = resolveArguments(method, message, context);
                try {
                    return method.invoke(handler, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    var cause = unwrapCause(e);
                    throw new RuntimeException(cause);
                }
            };
        };

        var factoryContext = new MessageHandlerContext(channelHandler.handlerType(), factory);

        concurrentDictionarySafeRegister(channel, factoryContext);
        publisher.submit(new MessageSubscribedEvent(channel, null, channelHandler.handlerType()));
    }

    // endregion

    // region 消息处理

    @Override
    public CompletableFuture<Object> handleAsync(String channel, String recipient, MessageEnvelope<?> envelope, MessageContext context) {
        var future = new CompletableFuture<>();

        try {
            var factories = handlerContainer.get(channel);

            if (factories == null || factories.isEmpty()) {
                throw new IllegalStateException(String.format("No handler for message %s on channel %s", context.getMessageId(), channel));
            }

            LOGGER.info(() -> String.format("Message '%s' is being handled on channel %s with %d handler(s)", context.getMessageId(), channel, factories.size()));

            if (!convention.isMulticast(channel)) {
                // 单个处理器 — 支持请求/响应模式
                var factory = factories.get(0);

                try {
                    var result = idempotentHandler.executeHandler(factory, envelope, context);
                    future.complete(result);
                } catch (Exception exception) {
                    future.completeExceptionally(exception);
                }
            } else {
                var handlers = factories.stream()
                                        .map(factory -> factory.handlerType().getTypeName())
                                        .toList();
                if (inboxStore != null && !inboxStore.insert(channel, envelope, handlers)) {
                    future.complete(null);
                } else {
                    // 多个处理器 — 并行执行所有（多播场景）
                    var futures = factories.stream()
                                           .map(factory -> idempotentHandler.executeHandlerAsync(factory, envelope, context))
                                           .toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(futures).whenComplete((result, exception) -> {
                        if (exception != null) {
                            future.completeExceptionally(exception);
                        } else {
                            future.complete(result);
                        }
                    });
                }
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            future.completeExceptionally(exception);
        }
        return future.whenComplete((result, error) -> {
            if (error != null) {
                LOGGER.log(Level.SEVERE, String.format("Message '%s' is being handled on channel %s with exception: %s", context.getMessageId(), channel, error.getMessage()), error);
                context.failure(error);
            } else {
                LOGGER.log(Level.INFO, String.format("Message '%s' is being handled on channel %s with result: %s", context.getMessageId(), channel, result));
                context.response(result);
            }
        });
    }

    // endregion

    // region 内部辅助方法

    /**
     * 安全地将值注册到值为列表的并发映射中。
     * <p>
     * 每个键的列表使用 {@link CopyOnWriteArrayList}，确保在 {@link #handleAsync}
     * 读取时不会与写操作发生竞态。
     *
     * @param key   要注册值所用的键
     * @param value 要添加到给定键对应列表中的值
     */
    private void concurrentDictionarySafeRegister(String key, MessageHandlerContext value) {
        handlerContainer.compute(key, (k, list) -> {
            if (list == null) {
                var newList = new CopyOnWriteArrayList<MessageHandlerContext>();
                newList.add(value);
                return newList;
            }
            list.add(value);
            return list;
        });
    }

    /**
     * 解析用于调用处理器方法的参数。
     * 该方法最多支持三个参数，参数位置按类型解析：
     * <ul>
     *   <li>匹配 {@link MessageContext} 的参数接收提供的 {@code context} 实例。</li>
     *   <li>其他任何参数接收 {@code message} 实例。</li>
     * </ul>
     *
     * @param method  表示要调用的处理器方法的 {@link Method}
     * @param message 要传递给处理器的消息对象
     * @param context 当需要时传递给处理器的 {@link MessageContext}
     * @return 与方法参数对应的参数数组
     */
    private static Object[] resolveArguments(Method method, Object message, MessageContext context) {
        var parameterTypes = method.getParameterTypes();
        var arguments = new Object[parameterTypes.length];

        switch (parameterTypes.length) {
            case 0 -> {
            }
            case 1 -> {
                if (parameterTypes[0] == MessageContext.class) {
                    arguments[0] = context;
                } else {
                    arguments[0] = message;
                }
            }
            case 2, 3 -> {
                arguments[0] = message;
                for (var i = 1; i < parameterTypes.length; i++) {
                    if (parameterTypes[i] == MessageContext.class) {
                        arguments[i] = context;
                    }
                }
            }
            default -> {
                return new Object[0];
            }
        }

        return arguments;
    }

    /**
     * 如果存在 {@link java.util.concurrent.CompletionException}，则解包其原因。
     */
    private static Throwable unwrapCause(Throwable ex) {
        var cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    // endregion
}
