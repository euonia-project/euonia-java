package com.euonia.osba.abstracts;

import com.euonia.reflection.PropertyInfo;

/**
 * 表示一个可以获取和设置属性的对象的契约。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface OperableProperty {
    /**
     * 根据提供的 PropertyInfo 获取属性的值。
     *
     * @param propertyInfo 表示要获取的属性的 PropertyInfo 对象。
     * @param <V>          属性值的类型。
     * @return 属性的值。
     */
    <V> V getProperty(PropertyInfo<V> propertyInfo);

    /**
     * 根据提供的 PropertyInfo 和值设置属性的值。
     *
     * @param propertyInfo 表示要设置的属性的 PropertyInfo 对象。
     * @param value        要为属性设置的值。
     * @param <V>          属性值的类型。
     */
    <V> void setProperty(PropertyInfo<V> propertyInfo, V value);
}
