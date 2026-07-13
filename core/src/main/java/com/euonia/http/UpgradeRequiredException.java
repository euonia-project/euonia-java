package com.euonia.http;

/**
 * HTTP 426 Upgrade Required 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UpgradeRequiredException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public UpgradeRequiredException(String message) {
        super(426, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public UpgradeRequiredException(String message, Throwable cause) {
        super(426, message, cause);
    }
}
