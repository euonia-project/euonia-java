package com.euonia.security;

import java.util.Collections;
import java.util.Map;

/**
 * 凭证异常，当用户凭证（如用户名或密码）出现问题时抛出。
 *
 * @author damon(zhaorong@outlook.com)
 */
@SuppressWarnings("unused")
public abstract class CredentialException extends RuntimeException {
    /** 导致异常的凭证 */
    private final Object credential;
    /** 异常相关的额外详细信息 */
    private final Map<String, Object> details = Collections.emptyMap();

    /**
     * 使用指定的凭证构造异常。
     *
     * @param credential 导致异常的凭证
     */
    public CredentialException(Object credential) {
        this.credential = credential;
    }

    /**
     * 使用指定的凭证和错误消息构造异常。
     *
     * @param credential 导致异常的凭证
     * @param message    错误描述
     */
    public CredentialException(Object credential, String message) {
        super(message);
        this.credential = credential;
    }

    /**
     * 使用指定的凭证、错误消息和原因构造异常。
     *
     * @param credential 导致异常的凭证
     * @param message    错误描述
     * @param cause      异常的根因
     */
    public CredentialException(Object credential, String message, Throwable cause) {
        super(message, cause);
        this.credential = credential;
    }

    /**
     * 获取导致异常的凭证。
     *
     * @return 导致异常的凭证
     */
    public Object getCredential() {
        return credential;
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
     * @return 当前 CredentialException 实例
     */
    public CredentialException with(String key, Object value) {
        details.put(key, value);
        return this;
    }
}
