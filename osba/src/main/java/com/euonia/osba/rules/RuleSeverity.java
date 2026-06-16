package com.euonia.osba.rules;

/**
 * 定义 OSBA 框架中规则的严重级别。
 * 这些级别表示规则违规的重要程度，可用于对规则评估过程中发现的问题进行分类和优先级排序。
 *
 * @author damon(zhaorong@outlook)
 */
public enum RuleSeverity {
    /**
     * 表示必须立即处理的严重问题。
     * 此级别用于若未解决可能导致重大问题或失败的违规行为。
     */
    ERROR,
    /**
     * 表示应当处理但不严重的警告。
     * 此级别用于若未解决可能导致潜在问题的违规行为。
     */
    WARNING,
    /**
     * 表示无需立即处理的信息性消息。
     * 此级别用于提供洞察或建议的违规行为。
     */
    INFORMATION,
    /**
     * 表示操作或验证成功。
     * 此级别用于确认行为正确或符合规范的违规行为。
     */
    SUCCESS
}
