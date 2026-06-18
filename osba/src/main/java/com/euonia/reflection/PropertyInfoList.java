package com.euonia.reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * PropertyInfoList 是一个扩展自 ArrayList 的类，用于存储 PropertyInfo 对象。它提供了一个线程安全的方法
 * getOrAdd 来获取或添加 PropertyInfo。
 * 该类还包含一个 Semaphore 用于控制对列表的访问，以确保线程安全。此外，它还具有锁定机制，可以通过 lock 和 unlock
 * 方法来控制列表的修改权限。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class PropertyInfoList extends ArrayList<PropertyInfo<?>> {
    private final Semaphore semaphore = new Semaphore(1);

    /**
     * 创建一个新的 PropertyInfoList 实例。
     */
    public PropertyInfoList() {
        super();
    }

    /**
     * 使用指定的列表创建一个新的 PropertyInfoList 实例。
     *
     * @param list 要包含的 PropertyInfo 对象列表
     */
    public PropertyInfoList(List<PropertyInfo<?>> list) {
        super(list);
    }

    private boolean locked = false;

    /**
     * 判断列表是否被锁定。
     *
     * @return 如果列表被锁定，则返回 true，否则返回 false
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * 锁定列表，禁止修改。
     */
    public void lock() {
        locked = true;
    }

    /**
     * 解锁列表，允许修改。
     */
    public void unlock() {
        locked = false;
    }

    /**
     * 获取或添加 PropertyInfo 对象。
     *
     * @param propertyInfo 要获取或添加的 PropertyInfo 对象
     * @return 已存在的 PropertyInfo 对象或新添加的 PropertyInfo 对象
     */
    public PropertyInfo<?> getOrAdd(PropertyInfo<?> propertyInfo) {
        try {
            semaphore.acquire();
            var existing = this.stream().filter(p -> p.getName().equals(propertyInfo.getName())).findFirst()
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
            this.add(propertyInfo);
            return propertyInfo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while trying to acquire semaphore.", e);
        } finally {
            semaphore.release();
        }
    }
}
