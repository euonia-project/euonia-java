package com.euonia.bus.consistency;

import com.euonia.core.ArgumentNullException;
import com.euonia.core.ArgumentOutOfRangeException;

/**
 * 发件箱消息的传输状态记录，跟踪每条消息在各个传输通道上的发送状态。
 * <p>
 * 包含传输名称、发送状态（待发送/成功/失败）、重试次数和错误信息。状态由内部枚举 {@link Status} 定义。
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
    private String name;

    /**
     * 发送状态（PENDING=待发送, SUCCESS=成功, FAILED=失败）
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
        this.messageId = messageId;
    }

    /**
     * 获取传输名称。
     *
     * @return 传输名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置传输名称。
     *
     * @param name 传输名称
     */
    public void setName(String name) {
        ArgumentNullException.throwIfNullOrEmpty(name, "name");
        this.name = name;
    }

    /**
     * 获取发送状态。
     *
     * @return 发送状态
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 设置发送状态。
     *
     * @param status 发送状态
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
        ArgumentOutOfRangeException.throwIfNegative(retryAttempts, "retryAttempts");
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
        ArgumentNullException.throwIfNullOrEmpty(error, "error");
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
