package com.euonia.osba.rules;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import com.euonia.reflection.PropertyInfo;

/**
 * 提供规则的基础实现，包括公共属性和方法。此类可被扩展以创建具体的规则实现。
 *
 * @author damon(zhaorong@outlook)
 */
public abstract class RuleBase implements Rule {
    private static final String NAME_PREFIX = "rule://";

    private final String name;

    private PropertyInfo<?> property;

    /**
     * 使用默认名称创建 RuleBase 类的新实例。名称将基于规则类的类型自动生成。
     */
    protected RuleBase() {
        name = generateName(getClass());
    }

    /**
     * 使用指定属性创建 RuleBase 类的新实例。名称将基于规则类的类型和属性名称自动生成。
     *
     * @param property 与规则关联的属性
     */
    protected RuleBase(PropertyInfo<?> property) {
        name = generateName(getClass(), property.getName());
        this.property = property;
    }

    /**
     * 使用指定属性和成员创建 RuleBase 类的新实例。名称将基于规则类的类型、属性名称和成员名称自动生成。
     *
     * @param property 与规则关联的属性
     * @param member   与规则关联的成员，通常是一个方法或字段，用于提供额外的上下文信息以生成规则名称
     */
    protected RuleBase(PropertyInfo<?> property, Member member) {
        name = generateName(getClass(), property.getName(), member.getName());
        this.property = property;
    }

    /**
     * 使用指定属性和名称创建 RuleBase 类的新实例。名称将基于规则类的类型、属性名称和提供的名称自动生成。
     *
     * @param property 与规则关联的属性
     * @param names    提供的名称，用于生成规则名称
     */
    protected RuleBase(PropertyInfo<?> property, String... names) {
        var combinedNames = new String[names.length + 1];
        combinedNames[0] = property.getName();
        System.arraycopy(names, 0, combinedNames, 1, names.length);
        name = generateName(getClass(), combinedNames);
        this.property = property;
    }

    /**
     * 获取规则的名称，通常基于规则类的类型和相关属性生成。名称用于唯一标识规则并提供上下文信息。
     *
     * @return 规则的名称
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取与规则关联的属性信息。此属性提供了规则应用的上下文，通常用于确定规则适用的对象属性。
     *
     * @return 与规则关联的属性信息
     */
    @Override
    public final PropertyInfo<?> getProperty() {
        return property;
    }

    /**
     * 获取规则的优先级。优先级用于确定在多个规则适用时的执行顺序。默认实现返回 0，表示中等优先级。子类可以覆盖此方法以提供不同的优先级值。
     *
     * @return 规则的优先级
     */
    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * 生成规则名称的辅助方法。名称基于规则类的类型和提供的名称生成，格式为 "rule://{typeName}/{name1}/{name2}/..."。此方法用于确保规则名称的一致性和唯一性。
     *
     * @param type  规则类的类型
     * @param names 用于生成规则名称的附加名称
     * @return 生成的规则名称
     */
    private static String generateName(Type type, String... names) {
        var typeName = type.getTypeName();
        return generateName(typeName, names);
    }

    /**
     * 生成规则名称的辅助方法。名称基于提供的类型名称和附加名称生成，格式为 "rule://{typeName}/{name1}/{name2}/..."。此方法用于确保规则名称的一致性和唯一性。
     *
     * @param typeName 规则类的类型名称
     * @param names    用于生成规则名称的附加名称
     * @return 生成的规则名称
     */
    private static String generateName(String typeName, String... names) {
        var builder = new StringBuilder(NAME_PREFIX + typeName);
        for (var name : names) {
            builder.append("/").append(name);
        }
        return builder.toString();
    }
}
