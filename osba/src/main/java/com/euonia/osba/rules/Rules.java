package com.euonia.osba.rules;

import com.euonia.annotation.Validation;
import com.euonia.osba.BusinessObject;
import com.euonia.osba.abstracts.RuleCheckable;
import com.euonia.reflection.PropertyInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 规则管理器，负责管理业务对象的验证规则执行，
 * 跟踪已违反的规则并在验证完成时通知监听器。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class Rules {

    /**
     * 待验证的目标对象
     */
    private final RuleCheckable target;

    /**
     * 验证完成监听器列表，线程安全
     */
    private final List<Consumer<BrokenRuleCollection>> validationCompleteListeners = new CopyOnWriteArrayList<>();

    /**
     * 已违反规则的集合
     */
    private final BrokenRuleCollection brokenRules = new BrokenRuleCollection();

    /**
     * 当前正在执行的规则列表，线程安全
     */
    private final List<Rule> runningRules = new CopyOnWriteArrayList<>();

    /**
     * 是否抑制规则检查
     */
    private volatile boolean suppressRuleChecking;

    /**
     * 是否有正在运行的规则
     */
    private volatile boolean hasRunningRules;

    /**
     * 构造方法，创建指定目标对象的规则管理器。
     *
     * @param target 待验证的目标对象
     */
    public Rules(RuleCheckable target) {
        this.target = target;
    }

    /**
     * 规则管理器，采用双重检查锁定实现延迟初始化
     */
    private volatile RuleManager ruleManager;

    /**
     * 获取目标对象的规则管理器，必要时进行初始化。
     *
     * @return 目标对象对应的规则管理器
     */
    public RuleManager getRuleManager() {
        if (ruleManager == null) {
            synchronized (this) {
                if (ruleManager == null) {
                    ruleManager = RuleManager.getRules(target.getClass());
                }
            }
        }
        return ruleManager;
    }

    /**
     * 向目标对象的规则管理器中添加验证规则。
     *
     * @param rule 要添加的验证规则
     */
    public void addRule(Rule rule) {
        if (rule != null) {
            getRuleManager().getRules().add(rule);
        }
    }

    /**
     * 获取规则检查过程中已识别的违反规则集合。
     *
     * @return 已违反规则的集合
     */
    public BrokenRuleCollection getBrokenRules() {
        return brokenRules;
    }

    /**
     * 基于当前的已违反规则集合判断目标对象是否有效。
     *
     * @return 若目标对象有效返回 true，否则返回 false
     */
    public boolean isValid() {
        return brokenRules.getErrorCount() == 0;
    }

    /**
     * 检查当前是否有正在为目标对象运行的规则。
     *
     * @return 若有规则正在运行返回 true，否则返回 false
     */
    public boolean hasRunningRules() {
        return hasRunningRules;
    }

    /**
     * 获取目标对象的规则检查当前是否被抑制。
     * 当规则检查被抑制时，不会执行任何规则，对象将被视为有效。
     *
     * @return 若规则检查被抑制返回 true，否则返回 false
     */
    public boolean isSuppressRuleChecking() {
        return suppressRuleChecking;
    }

    /**
     * 设置目标对象的规则检查当前是否被抑制。
     * 当规则检查被抑制时，不会执行任何规则，对象将被视为有效。
     *
     * @param suppressRuleChecking true 表示抑制规则检查，false 表示启用规则检查
     */
    public void setSuppressRuleChecking(boolean suppressRuleChecking) {
        this.suppressRuleChecking = suppressRuleChecking;
    }

    /**
     * 添加验证完成时的通知监听器。
     * 监听器将接收已违反规则的集合作为参数。
     *
     * @param listener 要添加的监听器
     */
    public void addValidationCompleteListener(Consumer<BrokenRuleCollection> listener) {
        if (listener != null) {
            validationCompleteListeners.add(listener);
        }
    }

    /**
     * 移除验证完成时的通知监听器。
     *
     * @param listener 要移除的监听器
     */
    public void removeValidationCompleteListener(Consumer<BrokenRuleCollection> listener) {
        validationCompleteListeners.remove(listener);
    }

    /**
     * 检查目标对象的所有验证规则，并相应更新已违反规则的集合。
     * 该方法返回受影响的属性名称列表。
     *
     * @return 受影响的属性名称列表
     */
    public List<String> checkObjectRules() {
        if (suppressRuleChecking || getRuleManager().getRules().isEmpty()) {
            return List.of();
        }

        hasRunningRules = true;
        brokenRules.clearRules();
        List<String> affectedProperties = new ArrayList<>();
        getRuleManager().getRules().stream()
                        .sorted(Comparator.comparingInt(Rule::getPriority))
                        .forEach(rule -> runRule(rule, affectedProperties));

        hasRunningRules = false;
        notifyValidationComplete();
        return affectedProperties.stream().distinct().toList();
    }

    private void runRule(Rule rule, List<String> affectedProperties) {
        RuleContext context = new RuleContext(ctx -> {
            synchronized (this) {
                getBrokenRules().add(ctx.getResults(), ctx.getRule().getProperty().getName());
                runningRules.remove(ctx.getRule());

                var properties = new ArrayList<PropertyInfo<?>>();

                if (ctx.getRule().getProperty() != null) {
                    properties.add(ctx.getRule().getProperty());
                }

                if (ctx.getRule().getRelatedProperties() != null) {
                    properties.addAll(ctx.getRule().getRelatedProperties());
                }

                for (var property : properties) {
                    if (runningRules.stream().noneMatch(r -> r.getName().equals(property.getName()))) {
                        target.ruleCheckComplete(property);
                    }
                }

                if (!hasRunningRules) {
                    target.allRulesComplete();
                }
            }
        }) {
            {
                setRule(rule);
                setTarget(target);
                if (rule.getProperty() == null) {
                    System.out.println(rule.getName());
                }
                setPropertyName(rule.getProperty().getName());
            }
        };
        String propertyName = rule.getProperty().getName();
        if (propertyName != null) {
            brokenRules.clearRules(propertyName);
            affectedProperties.add(propertyName);
        }
        affectedProperties.addAll(rule.getRelatedProperties().stream().map(PropertyInfo::getName).toList());

        runningRules.add(rule);
        rule.execute(context);
        context.complete();
    }

    private void notifyValidationComplete() {
        for (var listener : validationCompleteListeners) {
            listener.accept(brokenRules);
        }
    }

    public void addDataAnnotationRules() {

        if (!(target instanceof BusinessObject<?> businessObject)) {
            return;
        }

        var registeredProperties = businessObject.getFieldManager().getRegisteredProperties();
        if (registeredProperties == null) {
            return;
        }

        for (var property : target.getClass().getDeclaredFields()) {

            if (property.getType() == PropertyInfo.class) {
                continue;
            }

            var annotations = property.getAnnotations();
            for (var annotation : annotations) {
                var validation = annotation.annotationType().getAnnotation(Validation.class);
                if (validation != null) {
                    var propertyInfo = registeredProperties.stream()
                                                           .filter(p -> p.getField() != null && p.getField().getName().equals(property.getName()))
                                                           .findFirst()
                                                           .orElse(null);
                    if (propertyInfo == null) {
                        continue;
                    }
                    var validatorType = validation.validator();
                    var rule = new DataAnnotationRule<>(propertyInfo, annotation, validatorType);
                    addRule(rule);
                }
            }
        }
    }
}
