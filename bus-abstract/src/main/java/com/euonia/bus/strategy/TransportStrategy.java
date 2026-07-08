package com.euonia.bus.strategy;

/**
 * 定义传输策略的契约，该策略决定了消息在出站和入站操作中的处理方式。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface TransportStrategy {
    /**
     * 获取传输策略的名称。
     *
     * @return 传输策略的名称
     */
    String getName();

    /**
     * 判断传输策略是否允许在指定通道上发送出站消息。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @return 如果传输策略允许在指定通道上发送出站消息则返回 true，否则返回 false
     */
    boolean allowOutgoing(String channel, Class<?> messageType);

    /**
     * 判断传输策略是否允许在指定通道上接收入站消息。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @return 如果传输策略允许在指定通道上接收入站消息则返回 true，否则返回 false
     */
    boolean allowIncoming(String channel, Class<?> messageType);
}
