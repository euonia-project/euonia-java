package com.euonia.bus.event;

import com.euonia.bus.MessageContext;

/**
 * 消息接收事件，表示消息已被接收但尚未处理完成。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageReceivedEvent extends MessageProcessedEvent {
    /**
     * 使用消息和处理上下文构造消息接收事件。
     *
     * @param message 已接收的消息
     * @param context 处理上下文
     */
    public MessageReceivedEvent(Object message, MessageContext context) {
        super(message, context, MessageProcessType.RECEIVED);
    }
}
