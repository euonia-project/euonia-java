package com.euonia.reflection;

import com.euonia.osba.BusinessObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FieldDataManager 是一个管理业务对象字段数据的类。它维护一个字段数据的映射，并提供方法来获取、创建、设置和加载字段数据。
 * 该类还提供了一个静态方法来初始化业务对象类型的静态字段，以确保所有静态属性都被注册。
 *
 * @author damon(zhaorong@outlook)
 */
public class FieldDataManager {
    private final Map<String, FieldData<?>> fieldData = new HashMap<>();
    private final List<PropertyInfo<?>> properties;

    public FieldDataManager(Class<?> businessObjectType) {
        properties = createConsolidatedList(businessObjectType);
    }

    /**
     * 创建一个合并的属性列表，包含指定类型及其所有父类（直到 BusinessObject）的属性。
     *
     * @param type 要创建属性列表的类型
     * @return 包含指定类型及其所有父类属性的列表
     */
    private static List<PropertyInfo<?>> createConsolidatedList(Class<?> type) {
        var result = new ArrayList<PropertyInfo<?>>();

        var currentType = type;
        var hierarchy = new ArrayList<Type>();
        do {
            hierarchy.add(currentType);
            currentType = currentType.getSuperclass();
        } while (currentType != null && !(BusinessObject.class.isAssignableFrom(currentType)));

        for (var index = hierarchy.size() - 1; index >= 0; index--) {
            var source = PropertyInfoManager.getPropertyListCache(hierarchy.get(index));
            source.lock();
            result.addAll(source);
        }

        return result;
    }

    /**
     * 获取已注册的属性列表。
     *
     * @return 已注册的属性列表
     */
    public List<PropertyInfo<?>> getRegisteredProperties() {
        return properties;
    }

    /**
     * 获取指定名称的已注册属性。
     *
     * @param propertyName 属性名称
     * @return 已注册的属性
     * @throws IllegalArgumentException 如果属性未注册
     */
    public PropertyInfo<?> getRegisteredProperty(String propertyName) {
        return getRegisteredProperties().stream()
                .filter(p -> p.getName().equals(propertyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property '" + propertyName + "' is not registered."));
    }

    public FieldData<?> getFieldData(String fieldName) {
        // if (!fieldData.containsKey(fieldName)) {
        // throw new IllegalArgumentException(String.format("Field '%s' is not
        // registered.", fieldName));
        // }

        return fieldData.getOrDefault(fieldName, null);
    }

    /**
     * 获取指定属性的字段数据。
     *
     * @param <T>      属性的类型
     * @param property 属性信息
     * @return 指定属性的字段数据
     */
    @SuppressWarnings("unchecked")
    public <T> FieldData<T> getFieldData(PropertyInfo<T> property) {
        return (FieldData<T>) getFieldData(property.getName());
    }

    /**
     * 检索指定属性的字段数据，如果尚不存在则创建。此方法是同步的，以确保在访问和修改字段数据映射时的线程安全。
     *
     * @param <T>      属性的类型
     * @param property 要检索或创建字段数据的属性
     * @return 指定属性的字段数据
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> FieldData<T> getOrCreateFieldData(PropertyInfo<T> property) {
        if (!fieldData.containsKey(property.getName())) {
            var field = property.newFieldData(property.getName());
            fieldData.put(property.getName(), field);
        }
        return (FieldData<T>) fieldData.get(property.getName());
    }

    /**
     * 为指定的属性和值设置字段数据，如果字段数据尚不存在则创建。此方法是同步的，以确保在访问和修改字段数据映射时的线程安全。
     *
     * @param <T>      属性的类型
     * @param property 要设置字段数据的属性
     * @param value    要为字段数据设置的值
     */
    public <T> void setFieldData(PropertyInfo<T> property, T value) {
        FieldData<T> field = getOrCreateFieldData(property);
        field.setValue(value);
    }

    /**
     * 加载指定属性和值的字段数据，并将其标记为未更改。
     *
     * @param property 要加载字段数据的属性
     * @param value    要为字段数据设置的值
     * @param <T>      属性的类型
     * @return 已加载的字段数据
     */
    @SuppressWarnings("UnusedReturnValue")
    public <T> FieldData<T> loadFieldData(PropertyInfo<T> property, T value) {
        var field = getOrCreateFieldData(property);
        field.setValue(value);
        field.markAsUnchanged();
        return field;
    }

    /**
     * 移除与指定属性关联的字段数据（如果存在）。
     *
     * @param property 要移除字段数据的属性
     */
    public void removeFieldData(PropertyInfo<?> property) {
        fieldData.remove(property.getName());
    }

    /**
     * 检查指定字段名称的字段数据是否存在。
     *
     * @param fieldName 要检查的字段名称
     * @return 如果指定字段名称的字段数据存在则返回 true，否则返回 false
     */
    public boolean fieldExists(String fieldName) {
        return fieldData.containsKey(fieldName);
    }

    /**
     * 检查指定属性的字段数据是否存在。
     *
     * @param property 要检查的属性
     * @return 如果指定属性的字段数据存在则返回 true，否则返回 false
     */
    public boolean fieldExists(PropertyInfo<?> property) {
        return fieldExists(property.getName());
    }

    /**
     * 初始化指定业务对象类型的静态字段，以确保所有静态属性都被注册。
     *
     * @param businessObjectType 要为其初始化静态字段的业务对象类型
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public synchronized static void initStaticFields(Class<?> businessObjectType) {
        var fields = businessObjectType.getDeclaredFields();
        for (var field : fields) {
            if (field.getModifiers() == java.lang.reflect.Modifier.STATIC) {
                field.setAccessible(true);
                try {
                    field.get(null);
                } catch (IllegalAccessException e) {
                    //
                }
            }
        }
    }

    /**
     * 检查是否有任何字段数据处于忙碌状态。
     *
     * @return 如果有任何字段数据处于忙碌状态则返回 true，否则返回 false
     */
    public boolean isBusy() {
        return fieldData.values().stream().anyMatch(FieldData::isBusy);
    }
}
