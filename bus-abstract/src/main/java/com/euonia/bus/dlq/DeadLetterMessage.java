package com.euonia.bus.dlq;

import java.time.Instant;

import com.euonia.bus.RoutedMessage;

/**
 * 死信消息包装器，记录被移入死信队列的原始消息及失败原因。
 *
 * @param <T> 消息负载类型
 * @author damon(zhaorong@outlook.com)
 */
public final class DeadLetterMessage<T> {

    /** 原始消息 */
    private final RoutedMessage<T> originalMessage;
    /** 失败原因 */
    private final String reason;
    /** 异常类型名称 */
    private final String exceptionType;
    /** 异常详细信息 */
    private final String exceptionMessage;
    /** 移入死信队列的时间 */
    private final long timestamp;

    public DeadLetterMessage(RoutedMessage<T> originalMessage, Throwable error) {
        this.originalMessage = originalMessage;
        this.reason = error != null ? error.getMessage() : "unknown";
        this.exceptionType = error != null ? error.getClass().getName() : null;
        this.exceptionMessage = error != null ? error.toString() : null;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public RoutedMessage<T> getOriginalMessage() {
        return originalMessage;
    }

    public String getReason() {
        return reason;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("DeadLetterMessage{messageId=%s, reason='%s', timestamp=%d}",
            originalMessage.getMessageId(), reason, timestamp);
    }
}
