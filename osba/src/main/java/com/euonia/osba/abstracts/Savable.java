package com.euonia.osba.abstracts;

/**
 * 标记一个可以保存到数据库或其他持久化存储的对象。
 *
 * @param <T> 被保存对象的类型。
 * @author damon(zhaorong@outlook.com)
 */
public interface Savable<T> {
    /**
     * 保存对象的完整状态到数据库或其他持久化存储。
     *
     * @param newObject 要保存的对象
     */
    void saveComplete(T newObject);

    /**
     * 保存对象的状态到数据库或其他持久化存储。
     *
     * @param forceUpdate 是否强制更新
     * @return 保存后的对象
     */
    T save(boolean forceUpdate);
}
