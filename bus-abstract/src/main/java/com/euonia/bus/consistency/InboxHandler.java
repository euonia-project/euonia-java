package com.euonia.bus.consistency;

import com.euonia.core.ArgumentNullException;

/**
 * 收件箱（Inbox）消息的处理状态记录，跟踪每条消息被各处理器消费的执行状态。
 * <p>
 * 用于实现幂等消费，确保同一条消息不会被重复处理。
 * 状态由内部枚举 {@link Status} 定义。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class InboxHandler {
    /**
     * 关联的消息 ID
     */
    private String messageId;

    /**
     * 处理器的标识名称
     */
    private String name;

    /**
     * 处理状态（0=待处理, 1=成功, 2=失败）
     */
    private Status status;

    /**
     * 已重试次数
     */
    private int retryAttempts;

    /**
     * 最近一次错误信息
     */
    private String error;

    /**
     * 获取关联的消息 ID。
     *
     * @return 消息 ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * 设置关联的消息 ID。
     *
     * @param messageId 消息 ID
     */
    public void setMessageId(String messageId) {
        ArgumentNullException.throwIfNullOrEmpty(messageId, "messageId");
        this.messageId = messageId;
    }

    /**
     * 获取处理器的标识名称。
     *
     * @return 处理器名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置处理器的标识名称。
     *
     * @param name 处理器名称
     */
    public void setName(String name) {
        ArgumentNullException.throwIfNullOrEmpty(name, "name");
        this.name = name;
    }

    /**
     * 获取处理状态。
     *
     * @return 处理状态（PENDING=待处理, SUCCESS=成功, FAILED=失败）
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 设置处理状态。
     *
     * @param status 处理状态（PENDING=待处理, SUCCESS=成功, FAILED=失败）
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 获取已重试次数。
     *
     * @return 已重试次数
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * 设置已重试次数。
     *
     * @param retryAttempts 已重试次数
     */
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    /**
     * 获取最近一次错误信息。
     *
     * @return 最近一次错误信息
     */
    public String getError() {
        return error;
    }

    /**
     * 设置最近一次错误信息。
     *
     * @param error 最近一次错误信息
     */
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
