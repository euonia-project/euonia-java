package com.euonia.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 对象池提供者，用于根据提供的策略创建和管理对象池。
 * 它使用并发映射来缓存不同策略的对象池，确保每个策略只有一个关联的池。
 *
 * @author <a href="mailto:zhaorong@outlook.com>">damon(zhaorong@outlook.com)</a>
 */
public class ObjectPoolProvider {

    private static final ConcurrentMap<Class<?>, ObjectPool<?>> cache = new ConcurrentHashMap<>();

    /**
     * 使用默认容量创建或获取与给定策略关联的 {@link ObjectPool}。
     * 如果该策略的池已存在，则返回缓存的实例。
     *
     * @param <T>    池管理的对象类型
     * @param policy 对象池策略
     * @return 与策略关联的对象池
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectPool<T> create(ObjectPoolPolicy<T> policy) {
        return (ObjectPool<T>) cache.computeIfAbsent(policy.getClass(), k -> new ObjectPool<>(policy));
    }

    /**
     * 使用指定容量创建或获取与给定策略关联的 {@link ObjectPool}。
     * 如果该策略的池已存在，则返回缓存的实例。
     *
     * @param <T>    池管理的对象类型
     * @param policy 对象池策略
     * @param size   池容量
     * @return 与策略关联的对象池
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectPool<T> create(ObjectPoolPolicy<T> policy, int size) {
        return (ObjectPool<T>) cache.computeIfAbsent(policy.getClass(), k -> new ObjectPool<>(policy, size));
    }

    /**
     * 从缓存中移除与给定策略关联的对象池。
     *
     * @param policy 要移除其关联池的策略
     */
    public static void remove(ObjectPoolPolicy<?> policy) {
        cache.remove(policy.getClass());
    }
}
