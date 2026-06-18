package com.euonia.core;

/**
 * 对象池接口，定义了获取池容量、关联的策略以及获取和释放对象的方法。
 *
 * @param <T> 池管理的对象类型
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface ObjectPool<T> {

    /**
     * 获取对象池的容量，即池中可以同时存在的最大对象数量。
     *
     * @return 对象池的容量
     */
    int getCapacity();

    /**
     * 获取与对象池关联的策略。
     *
     * @return 对象池策略
     */
    ObjectPoolPolicy<T> getPolicy();

    /**
     * 获取一个对象池中的对象。
     *
     * @return 对象池中的对象
     */
    T acquire();

    /**
     * 释放一个对象池中的对象，将其返回到池中。
     *
     * @param obj 要释放的对象
     */
    void release(T obj);
}
