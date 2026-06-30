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

    /**
     * 原始消息
     */
    private final RoutedMessage<T> originalMessage;
    /**
     * 失败原因
     */
    private final String reason;
    /**
     * 异常类型名称
     */
    private final String exceptionType;
    /**
     * 异常详细信息
     */
    private final String exceptionMessage;
    /**
     * 移入死信队列的时间
     */
    private final long timestamp;

    /**
     * 构造方法，创建一个新的死信消息实例。
     *
     * @param originalMessage 原始消息
     * @param error           异常信息
     */
    public DeadLetterMessage(RoutedMessage<T> originalMessage, Throwable error) {
        this.originalMessage = originalMessage;
        this.reason = error != null ? error.getMessage() : "unknown";
        this.exceptionType = error != null ? error.getClass().getName() : null;
        this.exceptionMessage = error != null ? error.toString() : null;
        this.timestamp = Instant.now().toEpochMilli();
    }

    /**
     * 获取原始消息。
     *
     * @return 原始消息
     */
    public RoutedMessage<T> getOriginalMessage() {
        return originalMessage;
    }

    /**
     * 获取失败原因。
     *
     * @return 失败原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 获取异常类型名称。
     *
     * @return 异常类型名称
     */
    public String getExceptionType() {
        return exceptionType;
    }

    /**
     * 获取异常详细信息。
     *
     * @return 异常详细信息
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * 获取移入死信队列的时间戳。
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("DeadLetterMessage{messageId=%s, reason='%s', timestamp=%d}",
                             originalMessage.getMessageId(), reason, timestamp);
    }
}
