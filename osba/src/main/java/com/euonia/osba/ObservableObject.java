package com.euonia.osba;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.euonia.osba.abstracts.OperableProperty;
import com.euonia.osba.abstracts.TrackableObject;
import com.euonia.reflection.PropertyInfo;

/**
 * 表示一个可观察的业务对象，能够跟踪其编辑状态（新建、已修改、已删除）并管理其忙碌状态。
 * 该类提供方法来标记对象为新建、已修改或已删除，以及检查对象是否忙碌或可保存。
 * 它还实现了遵循对象规则和权限的属性访问方法。
 *
 * @param <T> 可观察对象的类型
 * @author damon(zhaorong@outlook)
 */
@SuppressWarnings("unused")
public abstract class ObservableObject<T extends ObservableObject<T>> extends BusinessObject<T> implements TrackableObject, OperableProperty {
    private ObjectEditState state;
    private boolean checkObjectRulesOnDelete;
    private final AtomicInteger busyCounter = new AtomicInteger();
    private final Object lock = new Object();
    private final List<Consumer<Boolean>> busyListeners = new CopyOnWriteArrayList<>();

    /**
     * 获取对象的当前编辑状态，指示对象是新建、已修改还是已删除。
     *
     * @return 对象的当前编辑状态
     */
    public final ObjectEditState getState() {
        return state;
    }

    private void setState(ObjectEditState state) {
        this.state = state;
    }

    /**
     * 检查对象是否为新建状态，即已创建但尚未保存到数据库。
     *
     * @return 如果对象为新建则返回 true，否则返回 false
     */
    @Override
    public final boolean isNew() {
        return state == ObjectEditState.NEW;
    }

    /**
     * 检查对象是否已被修改，即自上次保存到数据库以来已被修改。
     *
     * @return 如果对象已被修改则返回 true，否则返回 false
     */
    @Override
    public final boolean isChanged() {
        return state == ObjectEditState.CHANGED;
    }

    /**
     * 检查对象是否已被标记为删除，即已被标记待删除但尚未从数据库中移除。
     *
     * @return 如果对象已被标记为删除则返回 true，否则返回 false
     */
    @Override
    public final boolean isDeleted() {
        return state == ObjectEditState.DELETED;
    }

    /**
     * 返回一个值用于判断当对象被标记为删除时是否应检查对象规则。
     * 此标志可在标记对象为删除时设置，以指示在允许删除操作继续之前是否应评估任何业务规则。
     *
     * @return 如果删除时应检查对象规则则返回 true，否则返回 false
     */
    public final boolean isCheckObjectRulesOnDelete() {
        return checkObjectRulesOnDelete;
    }

    /**
     * 将对象标记为新建，表示该对象已创建但尚未保存到数据库。
     * 此方法将对象的编辑状态设置为 NEW，可用于跟踪变更并判断对象是否需要保存。
     */
    public final void markAsNew() {
        setState(ObjectEditState.NEW);
    }

    /**
     * 将对象标记为已修改，表示该对象自上次保存到数据库以来已被修改。
     * 此方法将对象的编辑状态设置为 CHANGED，可用于跟踪变更并判断对象是否需要保存。
     */
    public final void markAsChanged() {
        setState(ObjectEditState.CHANGED);
    }

    /**
     * 将对象标记为删除，表示该对象已被标记待删除但尚未从数据库中移除。
     * 此方法将对象的编辑状态设置为 DELETED，可用于跟踪变更并判断对象是否需要删除。
     */
    public final void markAsDeleted() {
        markAsDeleted(false);
    }

    /**
     * 将对象标记为删除，表示该对象已被标记待删除但尚未从数据库中移除。
     * 此方法将对象的编辑状态设置为 DELETED，可用于跟踪变更并判断对象是否需要删除。
     *
     * @param checkObjectRules 在允许删除操作继续之前是否检查对象规则
     */
    public final void markAsDeleted(boolean checkObjectRules) {
        setState(ObjectEditState.DELETED);
        checkObjectRulesOnDelete = checkObjectRules;
    }

    /**
     * 检查对象当前是否忙碌，即是否正在执行阻止其被保存或修改的操作。
     * 此方法同时检查对象自身的忙碌状态以及任何关联字段或规则的忙碌状态，以判断对象当前是否忙碌。
     *
     * @return 如果对象忙碌则返回 true，否则返回 false
     */
    @Override
    public boolean isBusy() {
        return isSelfBusy() || (getFieldManager().isBusy());
    }

    /**
     * 检查对象自身是否忙碌，即是否正在执行阻止其被保存或修改的操作。
     * 此方法检查对象自身的忙碌状态以及正在运行的规则的忙碌状态，以判断对象当前是否忙碌。
     *
     * @return 如果对象自身忙碌则返回 true，否则返回 false
     */
    public boolean isSelfBusy() {
        return busyCounter.get() > 0 || getRules().hasRunningRules();
    }

    /**
     * 检查对象是否可保存，即对象是否有效、有变更且不忙碌。
     * 此方法可用于判断对象是否可以保存到数据库。
     *
     * @return 如果对象可保存则返回 true，否则返回 false
     */
    @Override
    public boolean isSavable() {
        return isValid() && (hasChangedProperties() || isChanged()) && !isBusy();
    }

    /**
     * 将对象标记为忙碌，表示该对象正在执行阻止其被保存或修改的操作。
     * 此方法递增忙碌计数器，并在对象变为忙碌时通知所有监听器。
     */
    protected void markAsBusy() {
        var updateValue = busyCounter.incrementAndGet();

        if (updateValue == 1) {
            notifyBusyChanged(true);
        }
    }

    /**
     * 将对象标记为空闲，表示该对象不再执行阻止其被保存或修改的操作。
     * 此方法递减忙碌计数器，并在对象变为空闲时通知所有监听器。
     */
    protected void markAsIdle() {
        var updateValue = busyCounter.decrementAndGet();

        if (updateValue == 0) {
            notifyBusyChanged(false);
        }
    }

    /**
     * 订阅一个监听器，当对象的忙碌状态发生变化时接收通知。
     *
     * @param listener 要通知的监听器
     */
    public final void onBusyChanged(Consumer<Boolean> listener) {
        addBusyChangedListener(listener);
    }

    /**
     * 添加一个监听器，当对象的忙碌状态发生变化时接收通知。
     *
     * @param listener 要通知的监听器
     */
    public final void addBusyChangedListener(Consumer<Boolean> listener) {
        if (listener == null) {
            return;
        }
        busyListeners.add(listener);
    }

    /**
     * 移除一个监听器，不再接收对象忙碌状态变化的通知。
     *
     * @param listener 要移除的监听器
     */
    public final void removeBusyChangedListener(Consumer<Boolean> listener) {
        if (listener == null) {
            return;
        }
        busyListeners.remove(listener);
    }

    /**
     * 通知所有监听器对象的忙碌状态已发生变化。
     *
     * @param busy 对象当前的忙碌状态
     */
    private void notifyBusyChanged(boolean busy) {
        for (Consumer<Boolean> listener : busyListeners) {
            listener.accept(busy);
        }
    }

    // region 获取/设置属性

    /**
     * 获取属性值，遵循对象规则和权限。
     *
     * @param propertyName 属性名称
     * @param field        当前字段值
     * @param defaultValue 默认值
     * @param <V>          属性值的类型
     * @return 属性值，如果无法读取属性则返回默认值
     */
    protected <V> V getProperty(String propertyName, V field, V defaultValue) {
        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);
        if (isBypassingRuleCheck() || canReadProperty(propertyInfo, true)) {
            return field;
        }
        return defaultValue;
    }

    /**
     * 获取属性值，遵循对象规则和权限。
     *
     * @param propertyInfo 属性信息
     * @param field        当前字段值
     * @param <V>          属性值的类型
     * @return 属性值，如果无法读取属性则返回默认值
     */
    protected <V> V getProperty(PropertyInfo<V> propertyInfo, V field) {
        return getProperty(propertyInfo, field, propertyInfo.getDefaultValue());
    }

    /**
     * 获取属性值，遵循对象规则和权限。
     *
     * @param propertyInfo 属性信息
     * @param field        当前字段值
     * @param defaultValue 默认值
     * @param <V>          属性值的类型
     * @return 属性值，如果无法读取属性则返回默认值
     */
    protected <V> V getProperty(PropertyInfo<V> propertyInfo, V field, V defaultValue) {
        return getProperty(propertyInfo.getName(), field, defaultValue);
    }

    /**
     * 获取属性值，遵循对象规则和权限。
     *
     * @param property 要获取值的属性信息。
     * @param <V>      属性值的类型。
     * @return 属性值，如果无法读取属性则返回属性的默认值。
     */
    @Override
    public <V> V getProperty(PropertyInfo<V> property) {
        V value;
        if (isBypassingRuleCheck() || canReadProperty(property, true)) {
            value = readProperty(property);
        } else {
            value = property.getDefaultValue();
        }
        return value;
    }

    /**
     * 设置属性值，遵循对象规则和权限。
     *
     * @param property 要设置值的属性信息。
     * @param newValue 要设置的新值。
     * @param <V>      属性值的类型。
     */
    @Override
    public <V> void setProperty(PropertyInfo<V> property, V newValue) {
        if (!isBypassingRuleCheck() && !canWriteProperty(property, true)) {
            return;
        }

        V oldValue;
        var fieldData = getFieldManager().getFieldData(property);
        if (fieldData == null) {
            oldValue = property.getDefaultValue();
            getFieldManager().loadFieldData(property, newValue);
        } else {
            oldValue = fieldData.getValue();
        }

        loadPropertyValue(property, oldValue, newValue, !isBypassingRuleCheck());
    }
    // endregion

}
