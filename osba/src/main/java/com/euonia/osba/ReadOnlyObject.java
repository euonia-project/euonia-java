package com.euonia.osba;

import com.euonia.reflection.PropertyInfo;

/**
 * 表示一个只读的业务对象。
 * 该类继承自 BusinessObject，适用于创建后不应被修改的对象。
 * 它不提供任何额外的方法或属性，但作为一个标记，用于指示该对象是只读的。
 *
 * @param <T> 业务对象的类型，必须继承自 ReadOnlyObject
 * @author damon(zhaorong@outlook)
 */
public abstract class ReadOnlyObject<T extends ReadOnlyObject<T>> extends BusinessObject<T> {
    @Override
    protected boolean isBypassingRuleCheck() {
        return true;
    }

    /**
     * 获取属性的值。如果对象未绕过规则检查且属性不可读，则该方法将返回指定的默认值。
     *
     * @param propertyName 属性名称
     * @param field        属性的当前值
     * @param defaultValue 属性的默认值，如果对象未绕过规则检查且属性不可读，则返回该值
     * @param <V>          属性值的类型
     * @return 属性的值，如果对象未绕过规则检查且属性不可读，则返回 defaultValue
     */
    protected <V> V getProperty(String propertyName, V field, V defaultValue) {

        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);

        if (isBypassingRuleCheck() || canReadProperty(propertyInfo, true)) {
            return field;
        }
        return defaultValue;
    }

    /**
     * 获取属性的值。如果对象未绕过规则检查且属性不可读，则该方法将返回属性的默认值。
     *
     * @param propertyInfo 属性信息
     * @param field        属性的当前值
     * @param <V>          属性值的类型
     * @return 属性的值，如果对象未绕过规则检查且属性不可读，则返回属性的默认值
     */
    protected <V> V getProperty(PropertyInfo<V> propertyInfo, V field) {
        return getProperty(propertyInfo.getName(), field, propertyInfo.getDefaultValue());
    }

    /**
     * 获取属性的值。如果对象未绕过规则检查且属性不可读，则该方法将返回指定的默认值。
     *
     * @param propertyInfo 属性信息
     * @param field        属性的当前值
     * @param defaultValue 属性的默认值，如果对象未绕过规则检查且属性不可读，则返回该值
     * @param <V>          属性值的类型
     * @return 属性的值，如果对象未绕过规则检查且属性不可读，则返回 defaultValue
     */
    protected <V> V getProperty(PropertyInfo<V> propertyInfo, V field, V defaultValue) {
        return getProperty(propertyInfo.getName(), field, defaultValue);
    }

    /**
     * 获取属性的值。如果对象未绕过规则检查且属性不可读，则该方法将返回属性的默认值。
     *
     * @param propertyInfo 属性信息
     * @param <V>          属性值的类型
     * @return 属性的值
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
     * 设置属性的值。如果对象未绕过规则检查且属性不可写入，则该方法将直接返回而不做任何更改。
     *
     * @param propertyInfo 属性信息
     * @param newValue     要设置的新值
     * @param <V>          属性值的类型
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
