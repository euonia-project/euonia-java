package com.euonia.security;

/**
 * 未经授权访问异常，当尝试在未通过适当认证或授权的情况下访问资源或执行操作时抛出。
 * 表示用户缺乏访问所请求资源所需的凭据或权限。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UnauthorizedAccessException extends RuntimeException {
    /**
     * 使用指定的错误消息构造异常。
     *
     * @param message 错误描述
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * 使用指定的错误消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
