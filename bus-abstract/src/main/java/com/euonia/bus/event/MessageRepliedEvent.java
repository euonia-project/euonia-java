package com.euonia.bus.event;

/**
 * 消息已回复事件，包含回复的结果。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageRepliedEvent extends MessageProcessedEvent{
    /** 回复结果 */
    private final Object result;

    /**
     * 使用消息和回复结果构造消息已回复事件。
     *
     * @param message 原始消息
     * @param result  回复的结果
     */
    public MessageRepliedEvent(Object message, Object result) {
        super(message, null, MessageProcessType.REPLIED);
        this.result = result;
    }

    /**
     * 获取回复的结果。
     *
     * @return 回复的结果
     */
    public Object getResult() {
        return result;
    }
}
