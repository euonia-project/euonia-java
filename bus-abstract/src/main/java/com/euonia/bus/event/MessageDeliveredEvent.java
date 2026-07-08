package com.euonia.bus.event;

import com.euonia.bus.MessageContext;

/**
 * 消息已投递事件，表示消息已成功投递到目标传输。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageDeliveredEvent extends MessageProcessedEvent {
    /**
     * 使用消息和处理上下文构造消息已投递事件。
     *
     * @param message 已投递的消息
     * @param context 处理上下文
     */
    public MessageDeliveredEvent(Object message, MessageContext context) {
        super(message, context, MessageProcessType.DELIVERED);
    }
}
