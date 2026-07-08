package com.euonia.bus.consistency;

/**
 * 收件箱（Inbox）消息的处理状态记录，跟踪每条消息被各处理器消费的执行状态。
 * <p>
 * 用于实现幂等消费，确保同一条消息不会被重复处理。
 * 状态由内部枚举 {@link Status} 定义。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class InboxHandle {
    /**
     * 关联的消息 ID
     */
    private String messageId;
    /**
     * 处理器的标识名称
     */
    private String handler;
    /**
     * 处理状态（0=待处理, 1=成功, 2=失败）
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

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
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
     * 处理状态枚举。
     */
    public enum Status {
        /**
         * 待处理
         */
        PENDING(0),
        /**
         * 处理成功
         */
        SUCCESS(1),
        /**
         * 处理失败
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
