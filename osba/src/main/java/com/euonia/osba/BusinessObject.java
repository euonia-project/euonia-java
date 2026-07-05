package com.euonia.osba;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.euonia.osba.abstracts.RuleCheckable;
import com.euonia.osba.abstracts.UseBusinessContext;
import com.euonia.osba.rules.BrokenRuleCollection;
import com.euonia.osba.rules.LambdaRule;
import com.euonia.osba.rules.RequiredRule;
import com.euonia.osba.rules.Rule;
import com.euonia.osba.rules.RuleContext;
import com.euonia.osba.rules.RuleManager;
import com.euonia.osba.rules.Rules;
import com.euonia.reflection.FieldDataManager;
import com.euonia.reflection.PropertyInfo;
import com.euonia.reflection.PropertyInfoManager;
import com.euonia.security.UnauthorizedAccessException;

/**
 * 业务对象的抽象基类，提供属性管理、规则检查、授权和属性变更通知等核心功能。
 *
 * @param <B> 业务对象的具体类型
 * @author damon(zhaorong@outlook.com)
 */
public abstract class BusinessObject<B extends BusinessObject<B>> implements UseBusinessContext, RuleCheckable, AutoCloseable {
    private final List<PropertyInfo<?>> changedProperties = new ArrayList<>();
    private volatile FieldDataManager fieldManager;
    protected final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private BusinessContext businessContext;

    /**
     * 设置业务上下文。
     *
     * @param businessContext 业务上下文
     */
    @Override
    public void setBusinessContext(BusinessContext businessContext) {
        this.businessContext = businessContext;
        onBusinessContextSet();
        initialize();
        initializeRules();
    }

    /**
     * 获取业务上下文。
     *
     * @return 业务上下文
     */
    @Override
    public BusinessContext getBusinessContext() {
        return businessContext;
    }

    /**
     * 当业务上下文设置完成后调用。
     */
    protected void onBusinessContextSet() {

    }

    /**
     * 初始化业务对象。
     */
    protected void initialize() {

    }

    /**
     * 初始化业务对象的规则。
     */
    private void initializeRules() {
        var rulesOfType = RuleManager.getRules(this.getClass());
        if (rulesOfType.isInitialized()) {
            return;
        }
        synchronized (rulesOfType) {
            if (rulesOfType.isInitialized()) {
                return;
            }

            try {
                getRules().addDataAnnotationRules();
                addRules();
                rulesOfType.setInitialized(true);
            } catch (Exception e) {
                RuleManager.cleanRules(this.getClass());
                throw e;
            }
        }
    }

    /**
     * 获取字段数据管理器的实例。此方法使用双重检查锁定来确保线程安全地初始化字段数据管理器。
     *
     * @return 字段数据管理器的实例
     */
    public FieldDataManager getFieldManager() {
        if (fieldManager == null) {
            fieldManager = new FieldDataManager(this.getClass());
        }
        return fieldManager;
    }

    private volatile Rules rules;

    /**
     * 获取规则管理器的实例。此方法使用双重检查锁定来确保线程安全地初始化规则管理器。
     *
     * @return 规则管理器的实例
     */
    protected Rules getRules() {
        if (rules == null) {
            rules = new Rules(this);
        }
        return rules;
    }

    /**
     * 添加自定义规则。
     */
    protected void addRules() {

    }

    /**
     * 添加规则到规则管理器。
     *
     * @param rule 要添加的规则
     */
    protected void addRule(Rule rule) {
        getRules().addRule(rule);
    }

    /**
     * 添加Lambda规则到规则管理器。
     *
     * @param property 属性信息
     * @param rule     规则函数
     * @param message  错误消息
     * @param <V>      属性类型
     */
    protected <V> void addRule(PropertyInfo<V> property, BiFunction<V, RuleContext, Boolean> rule, String message) {
        addRule(LambdaRule.of(property).check(rule).message(message));
    }

    /**
     * 添加Lambda规则到规则管理器。
     *
     * @param property       属性信息
     * @param rule           规则函数
     * @param messageFactory 错误消息工厂
     * @param <V>            属性类型
     */
    protected <V> void addRule(PropertyInfo<V> property, BiFunction<V, RuleContext, Boolean> rule, Function<V, String> messageFactory) {
        addRule(LambdaRule.of(property).check(rule).message(messageFactory));
    }

    /**
     * 添加必填规则到规则管理器。
     *
     * @param property 属性信息
     * @param message  错误消息
     * @param <V>      属性类型
     */
    protected <V> void addRequiredRule(PropertyInfo<V> property, String message) {
        addRule(RequiredRule.of(property).message(message));
    }

    /**
     * 添加必填规则到规则管理器。
     *
     * @param property       属性信息
     * @param messageFactory 错误消息工厂
     * @param <V>            属性类型
     */
    protected <V> void addRequiredRule(PropertyInfo<V> property, Function<Object, String> messageFactory) {
        addRule(RequiredRule.of(property).message(messageFactory));
    }

    /**
     * 检查指定属性的规则，并在规则检查完成后触发属性变更事件。
     *
     * @param propertyInfo 属性信息
     * @param oldValue     属性的旧值
     * @param newValue     属性的新值
     */
    protected void checkPropertyRules(PropertyInfo<?> propertyInfo, Object oldValue, Object newValue) {
        var properties = getRules().checkObjectRules();
        onPropertyChanged(propertyInfo, oldValue, newValue);
    }

    // region 规则检查

    /**
     * 获取业务对象的规则检查结果。
     *
     * @return 业务对象的规则检查结果，如果对象有效，则返回 true；否则返回 false。
     */
    @Override
    public boolean isValid() {
        return getRules().isValid();
    }

    /**
     * 当规则检查完成时调用，触发属性变更事件。
     */
    @Override
    public void ruleCheckComplete(PropertyInfo<?> property) {
        onPropertyChanged(property, null, null);
    }

    /**
     * 当规则检查完成时调用，触发属性变更事件。
     */
    @Override
    public void ruleCheckComplete(String propertyName) {
        onPropertyChanged(propertyName, null, null);
    }

    /**
     * 当所有规则检查完成时调用，触发属性变更事件。
     */
    @Override
    public void allRulesComplete() {

    }

    /**
     * 暂停规则检查。
     */
    @Override
    public void suspendRuleChecking() {
        getRules().setSuppressRuleChecking(true);
    }

    /**
     * 恢复规则检查。
     */
    @Override
    public void resumeRuleChecking() {
        getRules().setSuppressRuleChecking(false);
    }

    /**
     * 获取破坏的规则集合。
     *
     * @return 破坏的规则集合
     */
    @Override
    public BrokenRuleCollection getBrokenRules() {
        return getRules().getBrokenRules();
    }

    // endregion

    // region 属性变更

    /**
     * 获取需要在属性变更时检查规则的属性列表。
     *
     * @return 需要在属性变更时检查规则的属性列表
     */
    protected List<String> getPropertiesToCheckRulesOnChanged() {
        return List.of();
    }

    /**
     * 获取已更改的属性列表。
     *
     * @return 已更改的属性列表
     */
    public List<PropertyInfo<?>> getChangedProperties() {
        return List.copyOf(changedProperties);
    }

    /**
     * 判断是否有已更改的属性。
     *
     * @return 如果有已更改的属性，则返回 true；否则返回 false
     */
    public boolean hasChangedProperties() {
        return !changedProperties.isEmpty();
    }

    /**
     * 添加属性变更监听器。
     *
     * @param listener 要添加的属性变更监听器
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * 移除属性变更监听器。
     *
     * @param listener 要移除的属性变更监听器
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * 当属性值发生变化时调用，触发属性变更事件。
     *
     * @param propertyName 属性的名称
     * @param oldValue     属性的旧值
     * @param newValue     属性的新值
     */
    protected void onPropertyChanged(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * 当属性值发生变化时调用，触发属性变更事件。
     *
     * @param propertyInfo 属性信息
     * @param oldValue     属性的旧值
     * @param newValue     属性的新值
     */
    protected void onPropertyChanged(PropertyInfo<?> propertyInfo, Object oldValue, Object newValue) {
        onPropertyChanged(propertyInfo.getName(), oldValue, newValue);
    }

    /**
     * 当属性值发生变化时调用，标记属性为已更改，并根据需要检查规则。
     *
     * @param propertyInfo 属性信息
     * @param oldValue     属性的旧值
     * @param newValue     属性的新值
     */
    protected final void propertyHasChanged(PropertyInfo<?> propertyInfo, Object oldValue, Object newValue) {
        if (!changedProperties.contains(propertyInfo)) {
            changedProperties.add(propertyInfo);
        }
        if (getPropertiesToCheckRulesOnChanged().contains(propertyInfo.getName())) {
            checkPropertyRules(propertyInfo, oldValue, newValue);
        } else {
            onPropertyChanged(propertyInfo, oldValue, newValue);
        }
    }
    // endregion

    // region 加载属性

    /**
     * 检查指定属性的字段数据是否存在。
     *
     * @param propertyInfo 要检查的属性信息
     * @return 如果指定属性的字段数据存在则返回 true，否则返回 false
     */
    public boolean fieldExists(PropertyInfo<?> propertyInfo) {
        return getFieldManager().fieldExists(propertyInfo);
    }

    /**
     * 检查指定字段名称的字段数据是否存在。
     *
     * @param fieldName 要检查的字段名称
     * @return 如果指定字段名称的字段数据存在则返回 true，否则返回 false
     */
    public boolean fieldExists(String fieldName) {
        var propertyInfo = getFieldManager().getRegisteredProperty(fieldName);
        return fieldExists(propertyInfo);
    }

    /**
     * 读取指定属性的信息。
     *
     * @param propertyInfo 属性的信息
     * @param <T>          属性的类型
     * @return 指定属性的信息
     */
    public <T> T readProperty(PropertyInfo<T> propertyInfo) {
        T result;
        var field = getFieldManager().getFieldData(propertyInfo);
        if (field != null) {
            result = field.getValue();
        } else {
            result = propertyInfo.getDefaultValue();
            getFieldManager().loadFieldData(propertyInfo, result);
        }
        return result;
    }

    /**
     * 读取指定名称的属性值。
     *
     * @param propertyName 属性的名称
     * @return 指定名称的属性值
     */
    public Object readProperty(String propertyName) {
        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);
        return readProperty(propertyInfo);
    }

    /**
     * 加载属性值，如果属性值已存在，则使用现有值；否则使用属性的默认值。
     *
     * @param propertyInfo 属性的信息
     * @param <T>          属性的类型
     */
    public final <T> void loadProperty(PropertyInfo<T> propertyInfo, T value) {
        T oldValue;
        var fieldData = getFieldManager().getFieldData(propertyInfo);
        if (fieldData == null) {
            oldValue = propertyInfo.getDefaultValue();
            getFieldManager().loadFieldData(propertyInfo, oldValue);
        } else {
            oldValue = fieldData.getValue();
        }
        loadPropertyValue(propertyInfo, oldValue, value, false);
    }

    /**
     * 加载属性值。
     *
     * @param propertyInfo  属性的信息
     * @param oldValue      旧值
     * @param newValue      新值
     * @param markAsChanged 是否标记为已更改
     */
    protected final <T> void loadPropertyValue(PropertyInfo<T> propertyInfo, T oldValue, T newValue, boolean markAsChanged) {
        var isValueChanged = !Objects.equals(oldValue, newValue);
        if (!isValueChanged) {
            return;
        }
        if (markAsChanged) {
            getFieldManager().setFieldData(propertyInfo, newValue);
            propertyHasChanged(propertyInfo, oldValue, newValue);
        } else {
            getFieldManager().loadFieldData(propertyInfo, newValue);
        }
    }

    /**
     * 判断两个属性值是否不同。
     *
     * @param propertyInfo 属性的信息
     * @param oldValue     旧值
     * @param newValue     新值
     * @return 如果属性值不同，返回 true；否则返回 false
     */
    protected final <T> boolean valuesDiffer(PropertyInfo<T> propertyInfo, T oldValue, T newValue) {
        boolean isDifferent;
        if (oldValue == null) {
            isDifferent = newValue != null;
        } else {
            if (BusinessObject.class.isAssignableFrom(propertyInfo.getType())) {
                //if (propertyInfo.getType().isAssignableFrom(BusinessObject.class)) {
                isDifferent = !Objects.equals(oldValue, newValue);
            } else {
                isDifferent = !oldValue.equals(newValue);
            }
        }
        return isDifferent;
    }
    // endregion

    // region 授权

    /**
     * 判断指定属性是否可读。
     *
     * @param propertyInfo 属性的信息
     * @return 如果属性可读，返回 true；否则返回 false
     */
    public boolean canReadProperty(PropertyInfo<?> propertyInfo) {
        return true;
    }

    /**
     * 判断指定名称的属性是否可读。
     *
     * @param propertyName 属性的名称
     * @return 如果属性可读，返回 true；否则返回 false
     */
    public boolean canReadProperty(String propertyName) {
        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);
        return canReadProperty(propertyInfo);
    }

    /**
     * 判断指定属性是否可读，并在属性不可读时抛出 UnauthorizedAccessException 异常。
     *
     * @param propertyInfo 属性的信息
     * @param throwOnFalse 如果为 true，当属性不可读时抛出异常
     * @return 如果属性可读，返回 true；否则返回 false
     */
    public final boolean canReadProperty(PropertyInfo<?> propertyInfo, boolean throwOnFalse) {
        var result = canReadProperty(propertyInfo);
        if (!result && throwOnFalse) {
            throw new UnauthorizedAccessException(String.format("属性 '%s' 不可读。", propertyInfo.getName()));
        }
        return result;
    }

    /**
     * 判断指定名称的属性是否可读，并在属性不可读时抛出 UnauthorizedAccessException 异常。
     *
     * @param propertyName 属性的名称
     * @param throwOnFalse 如果为 true，当属性不可读时抛出异常
     * @return 如果属性可读，返回 true；否则返回 false
     */
    public final boolean canReadProperty(String propertyName, boolean throwOnFalse) {
        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);
        return canReadProperty(propertyInfo, throwOnFalse);
    }

    /**
     * 判断指定属性是否可写。
     *
     * @param propertyInfo 属性的信息
     * @return 如果属性可写，返回 true；否则返回 false
     */
    public boolean canWriteProperty(PropertyInfo<?> propertyInfo) {
        return true;
    }

    /**
     * 判断指定属性是否可写，并在属性不可写时抛出 UnauthorizedAccessException 异常。
     *
     * @param propertyInfo 属性的信息
     * @param throwOnFalse 如果为 true，当属性不可写时抛出异常
     * @return 如果属性可写，返回 true；否则返回 false
     */
    public final boolean canWriteProperty(PropertyInfo<?> propertyInfo, boolean throwOnFalse) {
        var result = canWriteProperty(propertyInfo);
        if (!result && throwOnFalse) {
            throw new UnauthorizedAccessException(String.format("属性 '%s' 不可写。", propertyInfo.getName()));
        }
        return result;
    }

    /**
     * 判断指定名称的属性是否可写。
     *
     * @param propertyName 属性的名称
     * @return 如果属性可写，返回 true；否则返回 false
     */
    public boolean canWriteProperty(String propertyName) {
        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);
        return canWriteProperty(propertyInfo);
    }

    /**
     * 判断指定名称的属性是否可写，并在属性不可写时抛出 UnauthorizedAccessException 异常。
     *
     * @param propertyName 属性的名称
     * @param throwOnFalse 如果为 true，当属性不可写时抛出异常
     * @return 如果属性可写，返回 true；否则返回 false
     */
    public final boolean canWriteProperty(String propertyName, boolean throwOnFalse) {
        var propertyInfo = getFieldManager().getRegisteredProperty(propertyName);
        return canWriteProperty(propertyInfo, throwOnFalse);
    }
    // endregion

    // region 属性检查
    private boolean isBypassingRuleCheck = false;

    /**
     * 判断当前业务对象是否正在绕过规则检查。
     *
     * @return 如果正在绕过规则检查，则返回 true；否则返回 false。
     */
    protected boolean isBypassingRuleCheck() {
        return isBypassingRuleCheck;
    }

    private BypassRuleCheckObject<B> internalBypassRuleChecks;

    /**
     * 创建一个新的 BypassRuleCheckObject 实例，并将其与当前业务对象关联，以绕过规则检查。
     *
     * @return 创建的 BypassRuleCheckObject 实例
     */
    protected final BypassRuleCheckObject<B> bypassRuleChecks() {
        return BypassRuleCheckObject.create(this);
    }

    /**
     * 用于绕过规则检查的对象。
     *
     * @param <B> 业务对象的类型
     */
    protected static final class BypassRuleCheckObject<B extends BusinessObject<B>> implements AutoCloseable {

        private BusinessObject<B> businessObject;

        /**
         * 创建一个新的 BypassRuleCheckObject 实例，并将其与指定的业务对象关联。
         *
         * @param businessObject 要关联的业务对象
         */
        protected BypassRuleCheckObject(BusinessObject<B> businessObject) {
            this.businessObject = businessObject;
            this.businessObject.isBypassingRuleCheck = true;
        }

        /**
         * 创建一个新的 BypassRuleCheckObject 实例，并将其与指定的业务对象关联。
         *
         * @param businessObject 要关联的业务对象
         * @param <B>            业务对象的类型
         * @return 创建的 BypassRuleCheckObject 实例
         */
        public synchronized static <B extends BusinessObject<B>> BypassRuleCheckObject<B> create(BusinessObject<B> businessObject) {
            businessObject.internalBypassRuleChecks = new BypassRuleCheckObject<>(businessObject);
            businessObject.internalBypassRuleChecks.incrementRefCount();
            return businessObject.internalBypassRuleChecks;
        }

        /**
         * 关闭 BypassRuleCheckObject 实例，减少引用计数，并在引用计数为零时重置业务对象的规则检查状态。
         */
        @Override
        public void close() {
            decrementRefCount();
        }

        private int refCount = 0;

        /**
         * 增加引用计数。
         */
        private synchronized void incrementRefCount() {
            refCount++;
        }

        /**
         * 减少引用计数。
         */
        private synchronized void decrementRefCount() {
            refCount--;
            if (refCount != 0) {
                return;
            }

            this.businessObject.isBypassingRuleCheck = false;
            this.businessObject.internalBypassRuleChecks = null;
            this.businessObject = null;
        }

    }
    // endregion

    // region 注册属性

    /**
     * 使用指定的属性信息注册属性。
     *
     * @param propertyInfo 要注册的属性信息
     * @param <V>          属性的类型
     * @return 已注册的属性信息
     */
    @SuppressWarnings("unchecked")
    protected <V> PropertyInfo<V> registerProperty(PropertyInfo<V> propertyInfo) {
        return (PropertyInfo<V>) PropertyInfoManager.registerProperty(getClass(), propertyInfo);
    }

    /**
     * 使用指定的类型和名称注册属性。
     *
     * @param type         属性的类型
     * @param propertyName 属性的名称
     * @param <V>          属性的类型
     * @return 已注册的属性信息
     */
    protected <V> PropertyInfo<V> registerProperty(Class<V> type, String propertyName) {
        return registerProperty(new PropertyInfo<>(type, propertyName, null, getClass(), null));
    }

    /**
     * 使用指定的类型、名称和默认值注册属性。
     *
     * @param type         属性的类型
     * @param propertyName 属性的名称
     * @param defaultValue 属性的默认值提供者
     * @param <V>          属性的类型
     * @return 已注册的属性信息
     */
    protected <V> PropertyInfo<V> registerProperty(Class<V> type, String propertyName, Supplier<V> defaultValue) {
        return registerProperty(new PropertyInfo<>(type, propertyName, null, getClass(), defaultValue));
    }

    /**
     * 使用指定的类型、名称和友好名称注册属性。
     *
     * @param type         属性的类型
     * @param propertyName 属性的名称
     * @param friendlyName 属性的友好名称
     * @param <V>          属性的类型
     * @return 已注册的属性信息
     */
    protected <V> PropertyInfo<V> registerProperty(Class<V> type, String propertyName, String friendlyName) {
        return registerProperty(new PropertyInfo<>(type, propertyName, friendlyName, getClass(), null));
    }

    /**
     * 使用指定的类型、名称、友好名称和默认值注册属性。
     *
     * @param type         属性的类型
     * @param propertyName 属性的名称
     * @param friendlyName 属性的友好名称
     * @param defaultValue 属性的默认值
     * @param <V>          属性的类型
     * @return 已注册的属性信息
     */
    protected <V> PropertyInfo<V> registerProperty(Class<V> type, String propertyName, String friendlyName, V defaultValue) {
        return registerProperty(new PropertyInfo<>(type, propertyName, friendlyName, getClass(), () -> defaultValue));
    }

    /**
     * 使用指定的类型、名称、友好名称和默认值提供者注册属性。
     *
     * @param type         属性的类型
     * @param propertyName 属性的名称
     * @param friendlyName 属性的友好名称
     * @param defaultValue 属性的默认值提供者
     * @param <V>          属性的类型
     * @return 已注册的属性信息
     */
    protected <V> PropertyInfo<V> registerProperty(Class<V> type, String propertyName, String friendlyName, Supplier<V> defaultValue) {
        return registerProperty(new PropertyInfo<>(type, propertyName, friendlyName, getClass(), defaultValue));
    }
    // endregion


    @Override
    public void close() {

    }
}
