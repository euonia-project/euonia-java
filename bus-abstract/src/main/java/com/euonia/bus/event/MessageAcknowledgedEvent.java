package com.euonia.bus.event;

import com.euonia.bus.MessageContext;

/**
 * 消息确认事件，表示消息处理已完成并已确认。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageAcknowledgedEvent extends MessageProcessedEvent {
    /**
     * 使用消息和处理上下文构造消息确认事件。
     *
     * @param message 已确认的消息
     * @param context 处理上下文
     */
    public MessageAcknowledgedEvent(Object message, MessageContext context) {
        super(message, context, MessageProcessType.RECEIVED);
    }
}
