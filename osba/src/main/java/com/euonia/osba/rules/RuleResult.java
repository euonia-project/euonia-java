package com.euonia.osba.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.euonia.reflection.PropertyInfo;

/**
 * 表示规则执行的结果，包含规则是否通过、结果描述、严重级别以及所执行规则的名称。
 *
 * @author damon(zhaorong@outlook)
 */
public final class RuleResult {

    /**
     * 规则是否执行成功。当描述为 {@code null} 或空白时视为成功。
     */
    private final boolean success;

    /**
     * 规则执行结果的描述信息。成功时可能为 {@code null} 或空白，失败时包含失败原因。
     */
    private final String description;

    /**
     * 规则执行结果的严重级别。
     */
    private final RuleSeverity severity;

    /**
     * 执行产生此结果的规则名称。
     */
    private final String ruleName;

    /**
     * 与此规则结果关联的属性列表。
     */
    private final List<PropertyInfo<?>> properties = new ArrayList<>();

    /**
     * 构造一个成功的结果，仅包含规则名称。
     *
     * @param ruleName 规则名称
     */
    public RuleResult(String ruleName) {
        this(ruleName, null, RuleSeverity.SUCCESS);
    }

    /**
     * 使用指定的规则名称、描述和严重级别构造结果。
     * 若严重级别为 {@code null}，则默认为 {@link RuleSeverity#SUCCESS}；
     * 若描述为 {@code null} 或空白，则 {@link #isSuccess()} 返回 {@code true}。
     *
     * @param ruleName    规则名称
     * @param description 结果描述
     * @param severity    严重级别
     */
    public RuleResult(String ruleName, String description, RuleSeverity severity) {
        this.ruleName = ruleName;
        this.description = description;
        this.severity = severity == null ? RuleSeverity.SUCCESS : severity;
        this.success = description == null || description.isBlank();
    }

    /**
     * 判断规则执行是否成功。
     * 当没有失败描述（即描述为 {@code null} 或空白）时视为成功。
     *
     * @return 若规则执行成功则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取规则执行结果的描述信息。
     * 若规则执行成功，此值可能为 {@code null} 或空白；若失败，则包含失败描述。
     *
     * @return 规则执行结果的描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取规则执行结果的严重级别。
     * 严重级别表示规则执行结果的重要程度或影响，如成功、警告或错误。
     *
     * @return 规则执行结果的严重级别
     */
    public RuleSeverity getSeverity() {
        return severity;
    }

    /**
     * 获取执行产生此结果的规则名称。
     *
     * @return 所执行规则的名称
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * 获取与此规则结果关联的属性列表（不可修改）。
     * 可用于标识哪些属性受规则执行影响。
     *
     * @return 与此规则结果关联的属性列表
     */
    public List<PropertyInfo<?>> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    /**
     * 设置与此规则结果关联的属性列表。
     * 可用于标识哪些属性受规则执行影响。
     *
     * @param properties 要设置的属性列表
     */
    public void setProperties(List<PropertyInfo<?>> properties) {
        this.properties.clear();
        this.properties.addAll(properties);
    }
}
