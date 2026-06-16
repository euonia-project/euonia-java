package com.euonia.osba.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 表示在规则检查过程中被违反的已破坏规则的集合。
 * 提供管理已破坏规则集合的方法，包括添加新的已破坏规则、清除现有规则以及根据严重程度检索已破坏规则的数量。
 *
 * @author damon(zhaorong@outlook)
 */
public final class BrokenRuleCollection {
    private final List<BrokenRule> rules = new ArrayList<>();

    /**
     * 清除集合中的所有已破坏规则，无论其关联的属性如何。
     */
    public synchronized void clearRules() {
        rules.clear();
    }

    /**
     * 清除集合中与指定属性名称关联的已破坏规则。
     *
     * @param propertyName 应清除其关联已破坏规则的属性名称
     */
    public synchronized void clearRules(String propertyName) {
        rules.removeIf(rule -> propertyName == null
            ? rule.property() == null
            : propertyName.equals(rule.property()));
    }

    /**
     * 根据提供的规则结果列表和关联的属性名称，向集合中添加新的已破坏规则。
     *
     * @param results      要添加为已破坏规则的规则结果列表
     * @param propertyName 与已破坏规则关联的属性名称
     */
    public synchronized void add(List<RuleResult> results, String propertyName) {
        for (var result : results) {
            if (result.isSuccess()) {
                continue;
            }
            rules.add(new BrokenRule(propertyName, result.getDescription(), result.getSeverity()));
        }
    }

    /**
     * 检索集合中严重级别为 ERROR 的已破坏规则数量。
     *
     * @return 严重级别为 ERROR 的已破坏规则数量
     */
    public synchronized int getErrorCount() {
        return (int) rules.stream().filter(rule -> rule.severity() == RuleSeverity.ERROR).count();
    }

    /**
     * 检索集合中严重级别为 WARNING 的已破坏规则数量。
     *
     * @return 严重级别为 WARNING 的已破坏规则数量
     */
    public synchronized int getWarningCount() {
        return (int) rules.stream().filter(rule -> rule.severity() == RuleSeverity.WARNING).count();
    }

    /**
     * 检索集合中严重级别为 INFORMATION 的已破坏规则数量。
     *
     * @return 严重级别为 INFORMATION 的已破坏规则数量
     */
    public synchronized int getInformationCount() {
        return (int) rules.stream().filter(rule -> rule.severity() == RuleSeverity.INFORMATION).count();
    }

    /**
     * 检查已破坏规则集合是否为空。
     *
     * @return 如果集合为空则返回 true，否则返回 false
     */
    public synchronized boolean isEmpty() {
        return rules.isEmpty();
    }

    /**
     * 返回集合中已破坏规则的不可修改列表。
     *
     * @return 已破坏规则的不可修改列表
     */
    public synchronized List<BrokenRule> list() {
        return List.copyOf(rules);
    }

    /**
     * 返回集合中已破坏规则的流。
     *
     * @return 已破坏规则的流
     */
    public synchronized Stream<BrokenRule> stream() {
        return new ArrayList<>(rules).stream();
    }
}
