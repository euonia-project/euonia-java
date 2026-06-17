package com.euonia.bus.messenger;

import java.util.function.BiConsumer;

/**
 * 函数式接口，表示由特定接收者类型键控的、给定类型消息的处理器。
 *
 * @param <TRecipient> 处理消息的接收者类型
 * @param <TMessage>   消息类型
 * @author damon(zhaorong@outlook)
 */
@FunctionalInterface
public interface MessageHandler<TRecipient, TMessage> extends BiConsumer<TRecipient, TMessage> {

    /**
     * 为指定的接收者处理消息。
     *
     * @param recipient 接收者
     * @param message   消息
     */
    void handle(TRecipient recipient, TMessage message);

    @Override
    default void accept(TRecipient recipient, TMessage message) {
        handle(recipient, message);
    }
}
