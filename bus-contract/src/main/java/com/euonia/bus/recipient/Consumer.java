package com.euonia.bus.recipient;

/**
 * 消息消费者接口，继承自 {@link Recipient}。
 * <p>
 * 代表一个消息消费者，可以接收和处理消息。具体的消息处理逻辑由实现类定义，通常会通过 {@link com.euonia.bus.HandlerContext} 将消息分发给实际的消息处理器。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Consumer extends Recipient {
}
