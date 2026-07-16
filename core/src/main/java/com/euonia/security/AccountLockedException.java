package com.euonia.security;

/**
 * 账户锁定异常，表示账户由于某种原因被锁定，无法进行认证或授权操作。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class AccountLockedException extends AccountException {
    /**
     * 使用指定的账户标识构造账户锁定异常。
     *
     * @param identity 与错误关联的账户标识
     */
    public AccountLockedException(Object identity) {
        super(identity);
    }

    /**
     * 使用指定的账户标识和错误消息构造账户锁定异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     */
    public AccountLockedException(Object identity, String message) {
        super(identity, message);
    }

    /**
     * 使用指定的账户标识、错误消息和原因构造账户锁定异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     * @param cause    异常的根因
     */
    public AccountLockedException(Object identity, String message, Throwable cause) {
        super(identity, message, cause);
    }
}
