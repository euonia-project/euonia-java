package com.euonia.security;

/**
 * 认证异常，当认证因凭证无效、令牌过期或其他认证相关问题而失败时抛出。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class AuthenticationException extends RuntimeException {
    /**
     * 使用指定的错误消息构造异常。
     *
     * @param message 错误描述
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * 使用指定的错误消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
