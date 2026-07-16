package com.euonia.security;

import java.util.Collections;
import java.util.Map;

/**
 * 账户异常基类，用于认证或授权过程中的账户相关错误，
 * 如账户未找到、账户已锁定等。可通过子类扩展提供更具体的错误类型。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class AccountException extends RuntimeException {
    /** 账户标识（如用户名、用户 ID 或邮箱） */
    private final Object identity;

    /** 异常相关的额外详细信息 */
    private final Map<String, Object> details = Collections.synchronizedMap(new java.util.HashMap<>());

    /**
     * 使用指定的账户标识构造异常。
     *
     * @param identity 与错误关联的账户标识
     */
    public AccountException(Object identity) {
        this.identity = identity;
    }

    /**
     * 使用指定的账户标识和错误消息构造异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     */
    public AccountException(Object identity, String message) {
        super(message);
        this.identity = identity;
    }

    /**
     * 使用指定的账户标识、错误消息和原因构造异常。
     *
     * @param identity 与错误关联的账户标识
     * @param message  错误描述
     * @param cause    异常的根因
     */
    public AccountException(Object identity, String message, Throwable cause) {
        super(message, cause);
        this.identity = identity;
    }

    /**
     * 获取与错误关联的账户标识。
     *
     * @return 账户标识
     */
    public Object getIdentity() {
        return identity;
    }

    /**
     * 获取异常相关的额外详细信息。
     *
     * @return 详细信息的映射
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * 根据键从详细信息中获取特定的值。
     *
     * @param key 要检索的键
     * @return 与键关联的值，如果键不存在则返回 null
     */
    public Object get(String key) {
        return details.getOrDefault(key, null);
    }

    /**
     * 在详细信息中设置键值对。
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        details.put(key, value);
    }

    /**
     * 在详细信息中设置键值对，并返回当前实例支持链式调用。
     *
     * @param key   键
     * @param value 值
     * @return 当前 AccountException 实例
     */
    public AccountException with(String key, Object value) {
        details.put(key, value);
        return this;
    }
}
