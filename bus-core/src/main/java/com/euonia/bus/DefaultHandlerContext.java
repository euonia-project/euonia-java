package com.euonia.bus;

import java.lang.reflect.InvocationTargetException;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.convention.BaseMessageConvention;
import com.euonia.bus.event.MessageSubscribedEvent;
import com.euonia.bus.message.MessageCache;
import com.euonia.bus.message.MessageHandlerFactory;
import com.euonia.reflection.ServiceProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 默认的消息处理器上下文，使用 Euonia 的 {@link ServiceProvider} 进行依赖解析。
 * <p>
 * 该类管理处理器注册并将传入的消息分派到适当的处理器。
 * 它支持泛型处理器类型注册（通过 DI 解析）和基于反射的注册（通过 {@link HandlerRegistration}） 。
 *
 * @author damon(zhaorong@outlook.com)
 */
final class DefaultHandlerContext implements HandlerContext {

    private static final Logger LOGGER = Logger.getLogger(DefaultHandlerContext.class.getName());

    private final ConcurrentMap<String, List<MessageHandlerFactory>> handlerContainer = new ConcurrentHashMap<>();
    private final SubmissionPublisher<MessageSubscribedEvent> publisher = new SubmissionPublisher<>();
    private final ServiceProvider provider;
    private final MessageConvention convention;

    /**
     * 初始化 {@link DefaultHandlerContext} 的新实例。
     *
     * @param provider 用于解析处理器和其他服务的服务提供者
     */
    public DefaultHandlerContext(ServiceProvider provider) {
        this.provider = provider;
        this.convention = this.provider.getService(MessageConvention.class)
                                       .orElse(new BaseMessageConvention());
    }

    // region 事件订阅

    @Override
    public void onMessageSubscribed(Consumer<MessageSubscribedEvent> listener) {
        publisher.consume(listener);
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
     * @param messageType 消息的类
     * @param handlerType 处理器的类
     */
    public <M, R, H extends Handler<M, R>> void register(Class<M> messageType, Class<H> handlerType) {
        var channel = MessageCache.getInstance().getOrAddChannel(messageType);

        MessageHandlerFactory factory = sp -> {
            var handler = sp.getRequiredService(handlerType);
            return (message, context) -> {
                @SuppressWarnings("unchecked")
                var result = handler.handle((M) message, context);
                return result;
            };
        };

        concurrentDictionarySafeRegister(channel, factory);
        publisher.submit(new MessageSubscribedEvent(channel, messageType, handlerType));
    }

    /**
     * 注册由 {@link HandlerRegistration} 描述的处理器。
     * 该注册包含处理器类型、要调用的方法和通道名称。
     *
     * @param registration 描述要注册的处理器的 {@link HandlerRegistration}
     */
    public void register(HandlerRegistration registration) {
        MessageHandlerFactory factory = sp -> {
            var handler = sp.getServiceOrCreate(registration.handlerType());
            return (message, context) -> {
                var method = registration.method();
                var args = resolveArguments(method, message, context);
                try {
                    method.setAccessible(true);
                    return method.invoke(handler, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    var cause = unwrapCause(e);
                    throw new RuntimeException(cause);
                }
            };
        };

        concurrentDictionarySafeRegister(registration.channel(), factory);
        publisher.submit(new MessageSubscribedEvent(
            registration.channel(),
            registration.messageType(),
            registration.handlerType()));
    }

    // endregion

    // region 消息处理

    @Override
    public CompletableFuture<Void> handleAsync(Object message, MessageContext context) {
        if (message == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("message is null"));
        }

        var channel = MessageCache.getInstance().getOrAddChannel(message.getClass());
        return handleAsync(channel, message, context);
    }

    @Override
    public CompletableFuture<Void> handleAsync(String channel, Object message, MessageContext context) {
        try {
            if (message == null) {
                throw new IllegalArgumentException("message is null");
            }

            var factories = handlerContainer.get(channel);

            if (factories == null || factories.isEmpty()) {
                throw new IllegalStateException(String.format("No handler for message %s on channel %s", context.getMessageId(), channel));
            }

            LOGGER.info(() -> "Message " + context.getMessageId() + " is being handled");

            if (factories.size() == 1) {
                // 单个处理器 — 支持请求/响应模式
                var factory = factories.get(0);

                var task = executeHandler(factory, message, context);

                return task.<Void>handle((result, exception) -> {
                               if (exception != null) {
                                   var cause = unwrapCause(exception);
                                   context.failure(cause);
                               } else if (result != null) {
                                   context.response(result);
                               }
                               return null;
                           })
                           .whenComplete((v, exception) -> LOGGER.info(() -> "Message " + context.getMessageId() + " was completed handled"));
            } else {
                // 多个处理器 — 并行执行所有（多播场景）
                var futures = factories.stream()
                                       .map(factory -> executeHandler(factory, message, context)
                                           .thenAccept(result -> { /* 多播 — 结果被丢弃 */ }))
                                       .toArray(CompletableFuture[]::new);

                return CompletableFuture.allOf(futures)
                                        .whenComplete((v, ex) ->
                                            LOGGER.info(() -> "Message " + context.getMessageId() + " was completed handled"));
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            return CompletableFuture.failedFuture(exception);
        }
    }

    /**
     * 在给定的服务范围内执行单个处理器工厂。
     * 对于请求/单播消息类型，异常会被重新抛出。
     * 对于多播消息类型，异常会被吞没并作为结果返回。
     */
    private CompletableFuture<Object> executeHandler(MessageHandlerFactory factory, Object message, MessageContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var handler = factory.createHandler(provider);
                return handler.handle(message, context);
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, exception, () -> "Error occurred while handling message " + context.getMessageId());
                if (!convention.isMulticastType(message.getClass())) {
                    // 对于请求/响应和单播消息，吞没异常
                    throw new RuntimeException(exception);
                }
                return exception;
            }
        });
    }

    // endregion

    // region 内部辅助方法

    /**
     * 安全地将值注册到值为列表的并发映射中。
     * 该方法使用对内部处理器容器的锁，以确保对于组合的键/值操作，
     * 列表变更（添加/替换）是线程安全的。
     *
     * @param key   要注册值所用的键
     * @param value 要添加到给定键对应列表中的值
     */
    private void concurrentDictionarySafeRegister(String key, MessageHandlerFactory value) {
        synchronized (handlerContainer) {
            handlerContainer.compute(key, (k, list) -> {
                if (list == null) {
                    var newList = new ArrayList<MessageHandlerFactory>();
                    newList.add(value);
                    return newList;
                }
                if (!list.contains(value)) {
                    list.add(value);
                }
                return list;
            });
        }
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
            case 0:
                break;
            case 1:
                if (parameterTypes[0] == MessageContext.class) {
                    arguments[0] = context;
                } else {
                    arguments[0] = message;
                }
                break;
            case 2:
            case 3:
                arguments[0] = message;
                for (var i = 1; i < parameterTypes.length; i++) {
                    if (parameterTypes[i] == MessageContext.class) {
                        arguments[i] = context;
                    }
                }
                break;
            default:
                return new Object[0];
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
