package com.euonia.osba;

import com.euonia.reflection.PropertyInfo;

/**
 * Represents a read-only business object.
 * This class extends the BusinessObject class and is intended for objects that should not be modified after creation.
 * It does not provide any additional methods or properties, but serves as a marker to indicate that the object is read-only.
 *
 * @param <T> the type of the business object, which must extend ReadOnlyObject
 */
public abstract class ReadOnlyObject<T extends ReadOnlyObject<T>> extends BusinessObject<T> {
    @Override
    protected boolean isBypassingRuleCheck() {
        return true;
    }

    protected <V> V getProperty(String propertyName, V field, V defaultValue) {

        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);

        if (isBypassingRuleCheck() || canReadProperty(propertyInfo, true)) {
            return field;
        }
        return defaultValue;
    }

    protected <V> V getProperty(PropertyInfo<V> propertyInfo, V field) {
        return getProperty(propertyInfo.getName(), field, propertyInfo.getDefaultValue());
    }

    protected <V> V getProperty(PropertyInfo<V> propertyInfo, V field, V defaultValue) {
        return getProperty(propertyInfo.getName(), field, defaultValue);
    }

    /**
     * Gets the value of a property. If the object is not bypassing rule checks and the property cannot be read, the method will return the default value for the property.
     *
     * @param propertyInfo the property information
     * @param <V>          the type of the property value
     * @return the value of the property
     */
    protected <V> V getProperty(PropertyInfo<V> propertyInfo) {
        V value;
        if (isBypassingRuleCheck() || canReadProperty(propertyInfo, true)) {
            value = readProperty(propertyInfo);
        } else {
            value = propertyInfo.getDefaultValue();
        }
        return value;
    }

    /**
     * Sets the value of a property. If the object is not bypassing rule checks and the property cannot be written to, the method will return without making any changes.
     *
     * @param propertyInfo the property information
     * @param newValue     the new value to set
     * @param <V>          the type of the property value
     */
    protected <V> void setProperty(PropertyInfo<V> propertyInfo, V newValue) {
        if (!isBypassingRuleCheck() && !canWriteProperty(propertyInfo, true)) {
            return;
        }

        V oldValue;
        var fieldData = getFieldManager().getFieldData(propertyInfo);
        if (fieldData == null) {
            oldValue = propertyInfo.getDefaultValue();
            getFieldManager().loadFieldData(propertyInfo, newValue);
        } else {
            oldValue = fieldData.getValue();
        }

        loadPropertyValue(propertyInfo, oldValue, newValue, !isBypassingRuleCheck());
    }
}
