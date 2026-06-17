package com.euonia.bus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.messenger.WeakReferenceMessenger;

import com.euonia.bus.contract.Transport;
import com.euonia.bus.event.MessageDeliveredEvent;
import com.euonia.bus.event.MessageRepliedEvent;
import com.euonia.bus.messenger.Messenger;
import com.euonia.bus.messenger.StrongReferenceMessenger;

/**
 * 使用内存消息传递的 {@link Transport} 实现。
 * <p>
 * 消息通过 {@link Messenger}（默认为 {@link StrongReferenceMessenger}）进行分发，将 {@link MessagePack} 实例按通道路由到已注册的处理器。
 * <p>
 * 生命周期：实现 {@link AutoCloseable} 接口——调用 {@link #close()} 来重置底层 messenger（通常由 DI 容器管理）。
 *
 * @author damon(zhaorong@outlook)
 */
public class InMemoryTransport implements Transport, AutoCloseable {

    /**
     * 日志记录器。
     */
    private static final Logger log = Logger.getLogger(InMemoryTransport.class.getName());

    /**
     * 传输实例名称。
     */
    private final String name;

    /**
     * 消息已送达事件的监听器列表，使用写时复制策略以保证线程安全。
     */
    private final List<Consumer<MessageDeliveredEvent>> deliveredListeners = new CopyOnWriteArrayList<>();

    /**
     * 使用指定的配置选项初始化一个新的实例，
     * 使用默认的 {@link StrongReferenceMessenger} 单例。
     *
     * @param options 内存总线配置选项
     */
    public InMemoryTransport(InMemoryBusOptions options) {
        this.name = options.getName() != null ? options.getName() : InMemoryBusOptions.DEFAULT_TRANSPORT_NAME;
    }

    // ---- Transport 实现 ----

    @Override
    public String getName() {
        return name;
    }

    /**
     * 以即发即忘的方式发布消息。返回的 future 在分发后立即完成——不等待任何确认。
     *
     * @param message 要发布的消息
     * @param <M>     消息负载类型
     * @return 在分发后立即完成的 future
     */
    @Override
    public <M> CompletableFuture<Void> publishAsync(RoutedMessage<M> message) {
        MessageContextBase context = new MessageContextBase(message);
        MessagePack pack = new MessagePack(message, context);

        WeakReferenceMessenger.getDefault().send(pack, message.getChannel());
        fireDelivered(message.getPayload(), context);

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 发送一条命令消息并等待完成（或失败）。
     *
     * @param message 要发送的命令消息
     * @param <M>     消息负载类型
     * @return 在消息处理完毕时完成的 future
     */
    @Override
    public <M> CompletableFuture<Void> sendAsync(RoutedMessage<M> message) {
        MessageContextBase context = new MessageContextBase(message);
        MessagePack pack = new MessagePack(message, context);

        CompletableFuture<Void> future = new CompletableFuture<>();

        context.onReplied(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE); // 请求无限数量的事件
            }

            @Override
            public void onNext(MessageRepliedEvent event) {
                future.complete(null);
            }

            @Override
            public void onError(Throwable throwable) {
                log.log(Level.SEVERE, String.format("Message '%s' response failed with exception: %s", message.getMessageId(), throwable.getMessage()), throwable);
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
            }
        });

        context.addCompletedSubscriber(event -> {
            if (!future.isDone()) {
                future.complete(null);
            }
        });

        StrongReferenceMessenger.getDefault().send(pack, message.getChannel());
        fireDelivered(message.getPayload(), context);

        return future;
    }

    /**
     * 发送一条请求消息并期待一个类型化的响应。
     *
     * @param message      请求消息
     * @param responseType 期望的响应类型
     * @param <M>          请求负载类型
     * @param <R>          响应类型
     * @return 在收到响应时完成并携带响应的 future
     */
    @Override
    public <M, R> CompletableFuture<R> sendAsync(RoutedMessage<M> message, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();

        MessageContextBase context = new MessageContextBase(message);
        MessagePack pack = new MessagePack(message, context);
        context.onReplied(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE); // 请求无限数量的事件
            }

            @Override
            public void onNext(MessageRepliedEvent event) {
                log.info(String.format("Message '%s' responded with result: %s", message.getMessageId(),
                    event.getResult()));
                try {
                    future.complete(responseType.cast(event.getResult()));
                } catch (ClassCastException ex) {
                    future.completeExceptionally(ex);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.log(Level.SEVERE, String.format("Message '%s' response failed with exception: %s",
                    message.getMessageId(), throwable.getMessage()), throwable);
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
            }
        });

        context.addCompletedSubscriber(event -> {
            if (!future.isDone()) {
                future.complete(null);
            }
        });

        StrongReferenceMessenger.getDefault().send(pack, message.getChannel());
        fireDelivered(message.getPayload(), context);

        return future;
    }

    /**
     * 发送一条请求消息并期待一个响应。
     * <p>
     * 这是一个便利的重载方法，不指定编译时的期望响应类型。
     *
     * @param message 请求消息
     * @param <M>     请求负载类型
     * @param <R>     响应类型
     * @return 在收到响应时完成并携带响应的 future
     */
    @Override
    @SuppressWarnings("unchecked")
    public <M, R> CompletableFuture<R> requestAsync(RoutedMessage<M> message) {
        return (CompletableFuture<R>) sendAsync(message, Object.class);
    }

    // ---- 消息已送达事件（对应 .NET ITransport.Delivered） ----

    /**
     * 注册消息已送达事件的监听器。
     *
     * @param listener 要添加的监听器
     */
    public void addDeliveredListener(Consumer<MessageDeliveredEvent> listener) {
        deliveredListeners.add(listener);
    }

    /**
     * 取消注册消息已送达事件的监听器。
     *
     * @param listener 要移除的监听器
     */
    public void removeDeliveredListener(Consumer<MessageDeliveredEvent> listener) {
        deliveredListeners.remove(listener);
    }

    private void fireDelivered(Object payload, MessageContext context) {
        if (!deliveredListeners.isEmpty()) {
            MessageDeliveredEvent event = new MessageDeliveredEvent(payload, context);
            for (Consumer<MessageDeliveredEvent> listener : deliveredListeners) {
                listener.accept(event);
            }
        }
    }

    // ---- 生命周期 ----

    /**
     * 重置底层 messenger，清除所有已注册的处理器。
     * 这对应 .NET 的 {@code Dispose} 模式。
     */
    @Override
    public void close() {
        StrongReferenceMessenger.getDefault().reset();
    }
}
