package com.euonia.reflection;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PropertyInfoManager 是一个管理业务对象属性信息的类。它维护一个属性信息的缓存，并提供方法来获取、注册和查询属性信息。
 * 该类使用 ConcurrentHashMap 来存储不同类型的属性信息列表，并通过 computeIfAbsent
 * 方法确保每个类型只有一个属性信息列表被创建。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class PropertyInfoManager {
    private static final ConcurrentMap<Type, PropertyInfoList> propertyCache = new ConcurrentHashMap<>();

    /**
     * 获取指定类型的属性信息列表。如果列表不存在，则创建一个新的列表并缓存。
     *
     * @param type 要获取属性信息的类型
     * @return 指定类型的属性信息列表
     */
    public static PropertyInfoList getPropertyListCache(Type type) {
        return propertyCache.computeIfAbsent(type, t -> {
            PropertyInfoList propertyInfoList = new PropertyInfoList();
            FieldDataManager.initStaticFields((Class<?>) t);
            return propertyInfoList;
        });
    }

    /**
     * 获取指定类型的已注册属性信息列表。
     *
     * @param type 要获取已注册属性信息的类型
     * @return 指定类型的已注册属性信息列表
     */
    public static PropertyInfoList getRegisteredProperties(Type type) {
        var list = getPropertyListCache(type);
        return new PropertyInfoList(list);
    }

    /**
     * 获取指定类型和属性名称的已注册属性信息。
     *
     * @param type         要获取已注册属性信息的类型
     * @param propertyName 要获取已注册属性信息的属性名称
     * @return 指定类型和属性名称的已注册属性信息，如果不存在则返回 null
     */
    @SuppressWarnings("rawtypes")
    public static PropertyInfo getRegisteredProperty(Type type, String propertyName) {
        var properties = getRegisteredProperties(type);
        return properties.stream()
                .filter(property -> property.getName().equals(propertyName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 注册一个新的属性信息。如果属性信息已存在，则返回现有的属性信息。
     *
     * @param type         要注册属性信息的类型
     * @param propertyInfo 要注册的属性信息
     * @return 注册后的属性信息，如果属性信息已存在，则返回现有的属性信息
     */
    public static PropertyInfo<?> registerProperty(final Type type, PropertyInfo<?> propertyInfo) {
        var list = getPropertyListCache(type);
        return list.getOrAdd(propertyInfo);
    }
}
