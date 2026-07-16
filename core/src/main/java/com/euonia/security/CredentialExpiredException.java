package com.euonia.security;

/**
 * 凭证过期异常，表示提供的凭证（如密码、令牌等）已超过有效期，无法进行认证操作。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class CredentialExpiredException extends CredentialException {
    /**
     * 使用指定的账户标识构造凭证过期异常。
     *
     * @param identity 与错误关联的账户标识
     */
    public CredentialExpiredException(Object identity) {
        super(identity);
    }

    /**
     * 使用指定的账户标识和错误消息构造凭证过期异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     */
    public CredentialExpiredException(Object identity, String message) {
        super(identity, message);
    }

    /**
     * 使用指定的账户标识、错误消息和原因构造凭证过期异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     * @param cause    异常的根因
     */
    public CredentialExpiredException(Object identity, String message, Throwable cause) {
        super(identity, message, cause);
    }

}
