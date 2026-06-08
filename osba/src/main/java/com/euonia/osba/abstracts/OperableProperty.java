package com.euonia.osba.abstracts;

import com.euonia.reflection.PropertyInfo;

/**
 * Represents a contract for an object that can get and set properties.
 */
public interface OperableProperty {
    /**
     * Gets the value of a property based on the provided PropertyInfo.
     *
     * @param propertyInfo The PropertyInfo object representing the property to retrieve.
     * @param <V>          The type of the property value.
     * @return The value of the property.
     */
    <V> V getProperty(PropertyInfo<V> propertyInfo);

    /**
     * Sets the value of a property based on the provided PropertyInfo and value.
     *
     * @param propertyInfo The PropertyInfo object representing the property to set.
     * @param value        The value to set for the property.
     * @param <V>          The type of the property value.
     */
    <V> void setProperty(PropertyInfo<V> propertyInfo, V value);
}
