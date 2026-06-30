package com.euonia.bus;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.euonia.bus.event.MessageSubscribedEvent;

/**
 * 定义消息处理器上下文的契约。
 * <p>
 * 处理器上下文负责管理消息处理器的注册和消息的异步分发。
 * 支持消息订阅事件监听，以及基于消息类型或指定通道的异步消息处理。
 *
 * @author damon(zhaorong@outlook.com)
 * @see MessageContext
 * @see MessageSubscribedEvent
 */
public interface HandlerContext {

    /**
     * 添加消息订阅事件的监听器。
     *
     * @param listener 当消息被订阅时要调用的消费者
     */
    void onMessageSubscribed(Consumer<MessageSubscribedEvent> listener);

    /**
     * 异步处理消息，根据消息类型自动确定通道。
     *
     * @param message 要处理的消息
     * @param context 消息上下文
     * @return 表示操作待完成的 {@link CompletableFuture}
     */
    CompletableFuture<Object> handleAsync(Object message, MessageContext context);

    /**
     * 在指定通道上异步处理消息。
     *
     * @param channel 通道名称
     * @param message 要处理的消息
     * @param context 消息上下文
     * @return 表示操作待完成的 {@link CompletableFuture}
     */
    CompletableFuture<Object> handleAsync(String channel, Object message, MessageContext context);
}
