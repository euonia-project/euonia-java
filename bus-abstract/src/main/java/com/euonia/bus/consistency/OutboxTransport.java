package com.euonia.bus.consistency;

/**
 * 发件箱消息的传输状态记录，跟踪每条消息在各个传输通道上的发送状态。
 * <p>
 * 包含传输名称、发送状态（待发送/成功/失败）、重试次数和错误信息。
 * 状态由内部枚举 {@link Status} 定义。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class OutboxTransport {
    /**
     * 关联的消息 ID
     */
    private String messageId;
    /**
     * 传输名称
     */
    private String transport;
    /**
     * 发送状态（0=待发送, 1=成功, 2=失败）
     */
    private int status;
    /**
     * 已重试次数
     */
    private int retryAttempts;
    /**
     * 最近一次错误信息
     */
    private String error;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * 传输状态枚举。
     */
    public enum Status {
        /**
         * 待发送
         */
        PENDING(0),
        /**
         * 发送成功
         */
        SUCCESS(1),
        /**
         * 发送失败
         */
        FAILED(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        /**
         * 获取状态对应的整数值。
         *
         * @return 状态值
         */
        public int getValue() {
            return value;
        }
    }
}
