package com.euonia.osba.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 规则执行上下文，承载规则执行过程中的目标对象、当前规则、属性名和结果列表。
 * <p>
 * 规则执行完毕后，调用 {@link #complete()} 方法触发完成回调，若结果列表为空则自动添加成功结果。
 *
 * @author damon(zhaorong@outlook)
 */
public class RuleContext {

    /**
     * 规则执行过程中产生的结果列表。
     */
    private final List<RuleResult> results = new ArrayList<>();

    /**
     * 规则执行完成后的回调操作。
     */
    private final Consumer<RuleContext> completeAction;

    /**
     * 当前正在执行的规则。
     */
    private Rule rule;

    /**
     * 规则校验的目标对象。
     */
    private Object target;

    /**
     * 当前正在校验的属性名。
     */
    private String propertyName;

    /**
     * 使用指定的完成回调构造上下文。
     *
     * @param completeAction 规则执行完成后的回调操作
     */
    public RuleContext(Consumer<RuleContext> completeAction) {
        this.completeAction = completeAction;
    }

    /**
     * 获取当前正在执行的规则。
     *
     * @return 当前规则
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * 设置当前正在执行的规则。
     *
     * @param rule 要设置的规则
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * 获取规则校验的目标对象。
     *
     * @return 目标对象
     */
    public Object getTarget() {
        return target;
    }

    /**
     * 设置规则校验的目标对象。
     *
     * @param target 目标对象
     */
    public void setTarget(Object target) {
        this.target = target;
    }

    /**
     * 获取当前正在校验的属性名。
     *
     * @return 属性名
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 设置当前正在校验的属性名。
     *
     * @param propertyName 属性名
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * 获取规则执行的结果列表（不可修改）。
     *
     * @return 不可修改的结果列表
     */
    public List<RuleResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    /**
     * 添加一条严重级别为 {@link RuleSeverity#ERROR} 的错误结果。
     *
     * @param description 错误描述
     */
    public void addErrorResult(String description) {
        results.add(new RuleResult(rule.getName(), description, RuleSeverity.ERROR));
    }

    /**
     * 添加一条严重级别为 {@link RuleSeverity#WARNING} 的警告结果。
     *
     * @param description 警告描述
     */
    public void addWarningResult(String description) {
        results.add(new RuleResult(rule.getName(), description, RuleSeverity.WARNING));
    }

    /**
     * 添加一条严重级别为 {@link RuleSeverity#INFORMATION} 的信息结果。
     *
     * @param description 信息描述
     */
    public void addInformationResult(String description) {
        results.add(new RuleResult(rule.getName(), description, RuleSeverity.INFORMATION));
    }

    /**
     * 添加一条成功结果（无严重级别）。
     */
    public void addSuccessResult() {
        results.add(new RuleResult(rule.getName()));
    }

    /**
     * 完成规则执行，若结果列表为空则自动添加成功结果，然后触发完成回调。
     */
    public void complete() {
        if (results.isEmpty()) {
            addSuccessResult();
        }
        if (completeAction != null) {
            completeAction.accept(this);
        }
    }
}
