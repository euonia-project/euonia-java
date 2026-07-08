package com.euonia.bus.event;

/**
 * 消息已处理事件，表示消息已被处理器处理完成。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageHandledEvent extends MessageProcessedEvent {
    /**
     * 已处理的消息
     */
    private final Object message;
    /**
     * 处理该消息的处理器类型
     */
    private Class<?> handlerType;

    /**
     * 使用已处理的消息构造事件。
     *
     * @param message 已处理的消息
     */
    public MessageHandledEvent(Object message) {
        super(message, null, MessageProcessType.HANDLED);
        this.message = message;
    }

    /**
     * 获取处理该消息的处理器类型。
     *
     * @return 处理器类型
     */
    public Class<?> getHandlerType() {
        return handlerType;
    }

    /**
     * 设置处理该消息的处理器类型。
     *
     * @param handlerType 处理器类型
     */
    public void setHandlerType(Class<?> handlerType) {
        this.handlerType = handlerType;
    }

    /**
     * 返回已处理的消息。
     *
     * @return 消息对象
     */
    @Override
    public Object getMessage() {
        return message;
    }
}
