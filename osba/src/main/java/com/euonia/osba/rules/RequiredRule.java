package com.euonia.osba.rules;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.euonia.osba.BusinessObject;
import com.euonia.reflection.PropertyInfo;

/**
 * RequiredRule 类实现了一个验证规则，用于确保业务对象的特定属性具有非空值。
 * 该规则检查属性值是否为 null 或空字符串，如果验证失败，则将带有适当消息的错误结果添加到上下文中。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RequiredRule<T> extends RuleBase {

    private final PropertyInfo<T> property;
    private Function<Object, String> messageFactory;

    /**
     * 使用指定的属性和错误消息创建 RequiredRule 类的新实例。
     *
     * @param property 需要验证的属性
     * @param message  错误消息
     */
    public RequiredRule(PropertyInfo<T> property, String message) {
        this(property, x -> message);
    }

    /**
     * 使用指定的属性和消息工厂函数创建 RequiredRule 类的新实例。
     *
     * @param property       需要验证的属性
     * @param messageFactory 消息工厂函数，根据属性值生成适当的错误消息
     */
    public RequiredRule(PropertyInfo<T> property, Function<Object, String> messageFactory) {
        super(property);
        this.property = property;
        this.messageFactory = messageFactory;
    }

    /**
     * 执行规则的验证逻辑，检查目标对象是否为业务对象，检索属性值，并验证该值是否为 null 或空字符串。
     * 如果验证失败，则将带有适当消息的错误结果添加到上下文中。
     *
     * @param context 规则上下文，包含有关规则执行环境的信息，如目标对象和属性值。
     * @return 一个表示异步操作完成的 CompletableFuture
     */
    @Override
    public CompletableFuture<Void> executeAsync(RuleContext context) {
        try {
            if (context.getTarget() instanceof BusinessObject<?> businessObject) {

                Object value = businessObject.readProperty(property);

                if (value == null || (value instanceof String string && string.isEmpty())) {
                    context.addErrorResult(messageFactory.apply(value));
                }
            }
        } catch (Exception exception) {
            context.addErrorResult(exception.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 设置自定义的错误消息字符串，用于在验证失败时返回给用户。
     *
     * @param message 自定义的错误消息字符串
     * @return 当前的 RequiredRule 实例，以便进行链式调用
     */
    public RequiredRule<T> message(String message) {
        this.messageFactory = x -> message;
        return this;
    }

    /**
     * 设置自定义的消息工厂函数，用于根据属性值生成适当的错误消息。
     *
     * @param messageFactory 一个函数，接受属性值作为输入，并返回相应的错误消息字符串。
     * @return 当前的 RequiredRule 实例，以便进行链式调用。
     */
    public RequiredRule<T> message(Function<Object, String> messageFactory) {
        this.messageFactory = messageFactory;
        return this;
    }

    /**
     * 创建一个新的 RequiredRule 实例，绑定到指定的属性。
     *
     * @param property 要验证的属性信息
     * @param <T>      属性的类型
     * @return 一个新的 RequiredRule 实例
     */
    public static <T> RequiredRule<T> of(PropertyInfo<T> property) {
        return new RequiredRule<>(property, x -> String.format("Property '%s' is required.", property.getName()));
    }
}
