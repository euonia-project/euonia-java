package com.euonia.bus.event;

/**
 * 消息订阅事件，表示一个新处理器被订阅到某个消息通道。
 * 包含通道名称、消息类型和处理器类型。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageSubscribedEvent {

    /** 订阅的通道名称 */
    private final String channel;
    /** 消息类型 */
    private final Class<?> messageType;
    /** 处理器类型 */
    private final Class<?> handlerType;

    /**
     * 使用通道名、消息类型和处理器类型构造消息订阅事件。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @param handlerType 处理器类型
     */
    public MessageSubscribedEvent(String channel, Class<?> messageType, Class<?> handlerType) {
        this.channel = channel;
        this.messageType = messageType;
        this.handlerType = handlerType;
    }

    /**
     * 获取订阅的通道名称。
     *
     * @return 通道名称
     */
    public String getChannel() {
        return channel;
    }

    /**
     * 获取消息类型。
     *
     * @return 消息类型
     */
    public Class<?> getMessageType() {
        return messageType;
    }

    /**
     * 获取处理器类型。
     *
     * @return 处理器类型
     */
    public Class<?> getHandlerType() {
        return handlerType;
    }
}
