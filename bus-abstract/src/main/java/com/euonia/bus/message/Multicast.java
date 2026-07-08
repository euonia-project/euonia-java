package com.euonia.bus.message;

/**
 * {@link Multicast} 表示消息分发的逻辑通道。
 * 它允许消息被发布到多个订阅者，而无需发布者和订阅者之间的直接连接。
 * 在发布-订阅消息模式中通常使用主题，发布者将消息发送到主题，
 * 订阅者根据其订阅条件从该主题接收消息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Multicast extends Message {
}
