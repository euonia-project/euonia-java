package com.euonia.osba.abstracts;

import com.euonia.osba.rules.BrokenRuleCollection;
import com.euonia.reflection.PropertyInfo;

/**
 * 表示实现类具有规则检查功能。
 *
 * @author damon(zhaorong@outlook)
 */
public interface RuleCheckable {

    /**
     * 根据当前已破坏的规则指示对象是否有效。
     *
     * @return 如果对象有效则返回 true，否则返回 false
     */
    boolean isValid();

    /**
     * 指示对象的规则检查已完成，并且已破坏的规则集合已更新以反映当前状态。
     *
     * @param property 已完成规则检查的属性
     */
    void ruleCheckComplete(PropertyInfo<?> property);

    /**
     * 指示对象的规则检查已完成，并且已破坏的规则集合已更新以反映当前状态。
     *
     * @param propertyName 已完成规则检查的属性名称
     */
    void ruleCheckComplete(String propertyName);

    /**
     * 指示对象的规则检查已完成，并且已破坏的规则集合已更新以反映当前状态。
     */
    void allRulesComplete();

    /**
     * 暂停对象的规则检查。
     * 当规则检查被暂停时，对属性的更改将不会触发规则评估或更新已破坏的规则集合。
     * 当需要对对象执行多次更新，并希望在所有更改完成之前避免中间规则检查时，此功能非常有用。
     */
    void suspendRuleChecking();

    /**
     * 在暂停后恢复对象的规则检查。
     * 当规则检查恢复时，暂停期间对属性所做的任何更改都将触发规则评估，并且已破坏的规则集合将相应更新。
     * 这可以确保在规则检查暂停期间进行多次更改后，对象的有效性能够准确反映。
     */
    void resumeRuleChecking();

    /**
     * 获取对象的已破坏规则集合。
     *
     * @return 已破坏规则的集合
     */
    BrokenRuleCollection getBrokenRules();
}
