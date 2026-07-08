package com.euonia.bus.recipient;

/**
 * 消息订阅者接口，继承自 {@link Recipient}。
 * 用于多播/发布-订阅模式，接收发布到特定主题的消息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Subscriber extends Recipient {
}
