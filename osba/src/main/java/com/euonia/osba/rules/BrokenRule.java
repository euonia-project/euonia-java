package com.euonia.osba.rules;

/**
 * 表示在规则检查过程中被违反的已破坏规则。
 * 包含有关违反规则的属性、违规描述以及严重程度的信息。
 *
 * @author damon(zhaorong@outlook)
 */
public final class BrokenRule {
    private final String property;
    private final String description;
    private final RuleSeverity severity;

    public BrokenRule(String property, String description, RuleSeverity severity) {
        this.property = property;
        this.description = description;
        this.severity = severity;
    }

    public String getProperty() {
        return property;
    }

    public String getDescription() {
        return description;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }
}
