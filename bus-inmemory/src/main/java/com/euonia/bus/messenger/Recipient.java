package com.euonia.bus.messenger;

/**
 * 一个通用的消息接收者，可以接收类型为 {@code TMessage} 的消息。
 * <p>
 * 实现此接口的接收者可以通过优化的快速路径（null 分发器标记）注册到 {@link Messenger}。
 *
 * @param <TMessage> 此接收者处理的消息类型（必须是引用类型）
 * @author damon(zhaorong@outlook.com)
 */
@FunctionalInterface
public interface Recipient<TMessage> {

    /**
     * 当类型为 {@code TMessage} 的消息被发送到此接收者时调用。
     *
     * @param message 接收到的消息
     */
    void receive(TMessage message);
}
