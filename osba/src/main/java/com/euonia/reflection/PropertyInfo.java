package com.euonia.reflection;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Supplier;

import com.euonia.osba.BusinessObject;

/**
 * 表示业务对象属性的元数据，包括其名称、类型、默认值和显示名称。
 *
 * @param <T> 属性值的类型
 * @author damon(zhaorong@outlook)
 */
public class PropertyInfo<T> implements Comparable<PropertyInfo<T>> {
    private final Class<T> type;
    private final String name;
    private String friendlyName;
    private Supplier<T> defaultValue;
    private Field field;

    /**
     * 创建一个新的 PropertyInfo 实例。
     *
     * @param type 属性值的类型
     * @param name 属性的名称
     */
    public PropertyInfo(Class<T> type, String name) {
        this.name = name;
        this.type = type;
    }

    /**
     * 创建一个新的 PropertyInfo 实例，包含显示名称和默认值。
     *
     * @param type         属性值的类型
     * @param name         属性的名称
     * @param friendlyName 属性的显示名称
     * @param defaultValue 属性的默认值供应器
     */
    public PropertyInfo(Class<T> type, String name, String friendlyName, Supplier<T> defaultValue) {
        this(type, name);
        this.friendlyName = friendlyName;
        this.defaultValue = defaultValue;
    }

    /**
     * 创建一个新的 PropertyInfo 实例，包含显示名称、默认值和字段信息。
     *
     * @param type         属性值的类型
     * @param name         属性的名称
     * @param friendlyName 属性的显示名称
     * @param defaultValue 属性的默认值供应器
     * @param objectType   包含此属性的对象类型，用于反射获取字段信息
     */
    public PropertyInfo(Class<T> type, String name, String friendlyName, Class<?> objectType,
            Supplier<T> defaultValue) {
        this(type, name, friendlyName, defaultValue);
        try {
            this.field = objectType.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            //
        }
    }

    /**
     * 获取属性的名称。
     *
     * @return 属性的名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取属性的显示名称，如果未设置显示名称，则返回属性的名称。
     *
     * @return 属性的显示名称或属性的名称
     */
    public String getFriendlyName() {
        if (!Objects.isNull(friendlyName)) {
            return friendlyName;
        }

        if (field != null) {
            var annotation = field.getAnnotation(DisplayName.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return name;
    }

    /**
     * 获取属性的默认值。
     *
     * @return 属性的默认值
     */
    public T getDefaultValue() {
        return defaultValue == null ? null : defaultValue.get();
    }

    /**
     * 获取属性的类型。
     *
     * @return 属性的类型
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * 判断属性是否为子对象。
     *
     * @return 如果属性是子对象，则返回 true，否则返回 false
     */
    public boolean isChild() {
        return BusinessObject.class.isAssignableFrom(type);
    }

    /**
     * 获取属性对应的字段信息。
     *
     * @return 属性的字段信息
     */
    public Field getField() {
        return field;
    }

    /**
     * 创建一个新的 FieldData 实例。
     *
     * @param name 字段的名称
     * @return 新的 FieldData 实例
     */
    public FieldData<T> newFieldData(String name) {
        return new FieldData<>(name);
    }

    /**
     * 比较当前 PropertyInfo 与另一个 PropertyInfo 的顺序。
     *
     * @param other 要比较的另一个 PropertyInfo
     * @return 一个负整数、零或一个正整数，分别表示当前 PropertyInfo 小于、等于或大于指定的 PropertyInfo
     */
    @Override
    public int compareTo(PropertyInfo<T> other) {
        return this.name.compareTo(other.getName());
    }
}
