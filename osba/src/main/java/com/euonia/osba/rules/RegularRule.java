package com.euonia.osba.rules;

import java.util.function.Function;

import com.euonia.osba.BusinessObject;
import com.euonia.reflection.PropertyInfo;

/**
 * RegularRule 类实现了一个基于正则表达式的验证规则，用于验证业务对象的字符串属性是否符合指定的正则表达式模式。
 * 该规则允许配置是否忽略空值，并提供了一个消息工厂函数，用于根据属性值生成适当的错误消息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RegularRule extends RuleBase {

    private final PropertyInfo<String> property;
    private Function<String, String> messageFactory;
    private String expression;

    private boolean ignoreNullValue = true;

    public RegularRule(PropertyInfo<String> property) {
        super(property);
        this.property = property;
    }

    /**
     * 使用指定的属性、正则表达式和错误消息创建 RegularRule 类的新实例。
     *
     * @param property   需要验证的属性
     * @param expression 正则表达式模式
     * @param message    错误消息
     */
    public RegularRule(PropertyInfo<String> property, String expression, String message) {
        this(property);
        this.expression = expression;
        this.messageFactory = x -> message;
    }

    /**
     * 使用指定的属性、正则表达式和消息工厂函数创建 RegularRule 类的新实例。
     *
     * @param property       需要验证的属性
     * @param expression     正则表达式模式
     * @param messageFactory 消息工厂函数，根据属性值生成适当的错误消息
     */
    public RegularRule(PropertyInfo<String> property, String expression, Function<String, String> messageFactory) {
        this(property);
        this.expression = expression;
        this.messageFactory = messageFactory;
    }

    /**
     * 获取正则表达式。
     *
     * @return 正则表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 是否忽略空值。
     *
     * @return 是否忽略空值
     */
    public boolean isIgnoreNullValue() {
        return ignoreNullValue;
    }

    /**
     * 设置是否忽略空值。
     *
     * @param ignoreNullValue 是否忽略空值
     */
    public void setIgnoreNullValue(boolean ignoreNullValue) {
        this.ignoreNullValue = ignoreNullValue;
    }

    /**
     * 执行规则的验证逻辑，检查目标对象是否为业务对象，检索属性值，并根据配置的正则表达式验证该值。
     * 如果验证失败，则将带有适当消息的错误结果添加到上下文中。
     *
     * @param context 规则上下文，包含有关规则执行环境的信息，如目标对象和属性值。
     */
    @Override
    public void execute(RuleContext context) {
        try {
            if (context.getTarget() instanceof BusinessObject<?> businessObject) {

                String value = businessObject.readProperty(property);

                if (value == null || value.isEmpty()) {
                    if (!ignoreNullValue) {
                        context.addErrorResult(messageFactory.apply(value));
                    }
                } else {
                    if (!value.matches(expression)) {
                        context.addErrorResult(messageFactory.apply(value));
                    }
                }
            }
        } catch (Exception exception) {
            context.addErrorResult(exception.getMessage());
        }
    }

    public RegularRule expression(String expression) {
        this.expression = expression;
        return this;
    }

    public RegularRule message(Function<String, String> messageFactory) {
        this.messageFactory = messageFactory;
        return this;
    }

    public RegularRule message(String message) {
        this.messageFactory = (x) -> message;
        return this;
    }

    public RegularRule ignoreNullValue(boolean ignoreNullValue) {
        this.ignoreNullValue = ignoreNullValue;
        return this;
    }

    public static RegularRule of(PropertyInfo<String> property) {
        return new RegularRule(property);
    }
}
