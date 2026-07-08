package com.euonia.bus.event;

import com.euonia.bus.MessageContext;

/**
 * 消息处理事件的基类，封装消息处理过程中的通用状态。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageProcessedEvent {
    /**
     * 处理的消息
     */
    private final Object message;
    /**
     * 消息处理上下文
     */
    private final MessageContext context;
    /**
     * 处理类型
     */
    private final MessageProcessType processType;

    /**
     * 使用消息、上下文和处理类型构造事件。
     *
     * @param message     处理的消息
     * @param context     消息处理上下文
     * @param processType 处理类型
     */
    public MessageProcessedEvent(Object message, MessageContext context, MessageProcessType processType) {
        this.message = message;
        this.context = context;
        this.processType = processType;
    }

    /**
     * 获取处理的消息。
     *
     * @return 消息对象
     */
    public Object getMessage() {
        return message;
    }

    /**
     * 获取消息处理上下文。
     *
     * @return 处理上下文
     */
    public MessageContext getContext() {
        return context;
    }

    /**
     * 获取处理类型。
     *
     * @return 处理类型
     */
    public MessageProcessType getProcessType() {
        return processType;
    }
}
