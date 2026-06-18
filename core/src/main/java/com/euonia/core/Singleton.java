package com.euonia.core;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Singleton 是一个线程安全的单例工厂类，用于管理和提供全局唯一的实例。
 * 它使用 ConcurrentHashMap 来存储不同类型的实例，并通过 computeIfAbsent 方法确保每个类型只有一个实例被创建。
 * 该类提供了两种获取实例的方法：一种是通过 Class 对象直接获取，另一种是通过 Supplier 提供实例创建逻辑。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class Singleton {
    private static final ConcurrentHashMap<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private Singleton() {
        // 私有构造函数，禁止实例化
    }

    /**
     * 获取指定类型的单例实例。如果实例不存在，则使用反射创建一个新的实例。
     *
     * @param clazz 要获取实例的类对象
     * @param <T>   实例的类型
     * @return 指定类型的单例实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz) {
        return (T) instances.computeIfAbsent(clazz, Singleton::createInstance);
    }

    /**
     * 获取指定类型的单例实例。如果实例不存在，则使用提供的 Supplier 创建一个新的实例。
     *
     * @param clazz    要获取实例的类对象
     * @param supplier 用于创建实例的 Supplier
     * @param <T>      实例的类型
     * @return 指定类型的单例实例
     */
    private static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException
                | InvocationTargetException exception) {
            throw new RuntimeException("创建实例失败：" + clazz.getName(), exception);
        }
    }

    /**
     * 获取指定类型的单例实例。如果实例不存在，则使用提供的 Supplier 创建一个新的实例。
     *
     * @param clazz    要获取实例的类对象
     * @param supplier 用于创建实例的 Supplier
     * @param <T>      实例的类型
     * @return 指定类型的单例实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Supplier<T> supplier) {
        return (T) instances.computeIfAbsent(clazz, k -> supplier.get());
    }
}
