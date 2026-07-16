package com.euonia.security;

/**
 * 凭证未找到异常，表示指定的凭证在系统中不存在，通常用于认证过程中。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class CredentialNotFoundException extends CredentialException {
    /**
     * 使用指定的账户标识构造凭证未找到异常。
     *
     * @param identity 与错误关联的账户标识
     */
    public CredentialNotFoundException(Object identity) {
        super(identity);
    }

    /**
     * 使用指定的账户标识和错误消息构造凭证未找到异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     */
    public CredentialNotFoundException(Object identity, String message) {
        super(identity, message);
    }

    /**
     * 使用指定的账户标识、错误消息和原因构造凭证未找到异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     * @param cause    异常的根因
     */
    public CredentialNotFoundException(Object identity, String message, Throwable cause) {
        super(identity, message, cause);
    }

}
