package com.euonia.security;

/**
 * 凭证不正确异常，表示提供的凭证（如密码、令牌等）与账户不匹配，通常用于认证过程中。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class CredentialIncorrectException extends CredentialException {
    /**
     * 使用指定的账户标识构造凭证不正确异常。
     *
     * @param identity 与错误关联的账户标识
     */
    public CredentialIncorrectException(Object identity) {
        super(identity);
    }

    /**
     * 使用指定的账户标识和错误消息构造凭证不正确异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     */
    public CredentialIncorrectException(Object identity, String message) {
        super(identity, message);
    }

    /**
     * 使用指定的账户标识、错误消息和原因构造凭证不正确异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     * @param cause    异常的根因
     */
    public CredentialIncorrectException(Object identity, String message, Throwable cause) {
        super(identity, message, cause);
    }
}
