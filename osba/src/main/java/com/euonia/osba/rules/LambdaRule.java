package com.euonia.osba.rules;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.euonia.core.ArgumentNullException;
import com.euonia.osba.BusinessObject;
import com.euonia.reflection.PropertyInfo;

/**
 * LambdaRule 允许使用 lambda 函数定义验证规则，为实现业务对象特定属性的自定义验证逻辑提供了灵活性。
 *
 * @param <T> 被验证的属性值的类型
 * @author damon(zhaorong@outlook.com)
 */
public class LambdaRule<T> extends RuleBase {
    private final PropertyInfo<T> property;
    private BiFunction<T, RuleContext, Boolean> function;
    private Function<T, String> messageFactory;

    public LambdaRule(PropertyInfo<T> property) {
        super(property);
        this.property = property;
    }

    public LambdaRule(PropertyInfo<T> property, BiFunction<T, RuleContext, Boolean> function, String message) {
        this(property);
        ArgumentNullException.throwIfNull(function, "function");
        ArgumentNullException.throwIfNullOrEmpty(message, "message");
        this.function = function;
        this.messageFactory = (x) -> message;
    }

    public LambdaRule(PropertyInfo<T> property, BiFunction<T, RuleContext, Boolean> function, Function<T, String> messageFactory) {
        this(property);
        ArgumentNullException.throwIfNull(function, "function");
        ArgumentNullException.throwIfNull(messageFactory, "messageFactory");
        this.function = function;
        this.messageFactory = messageFactory;
    }

    /**
     * 执行规则的验证逻辑，检查目标对象是否为业务对象，检索属性值，并使用提供的 lambda 函数验证该值。
     * 如果验证失败，则将带有适当消息的错误结果添加到上下文中。
     *
     * @param context 规则上下文，包含有关规则执行环境的信息，如目标对象和属性值。
     */
    @Override
    public void execute(RuleContext context) {
        try {
            if (context.getTarget() instanceof BusinessObject<?> businessObject) {

                T value = businessObject.readProperty(property);

                if (!function.apply(value, context)) {
                    context.addErrorResult(messageFactory.apply(value));
                }
            }
        } catch (Exception exception) {
            context.addErrorResult(exception.getMessage());
        }
    }

    /**
     * 创建一个新的 LambdaRule 实例，绑定到指定的属性。
     *
     * @param property 要验证的属性信息
     * @param <T>      属性的类型
     * @return 一个新的 LambdaRule 实例
     */
    public static <T> LambdaRule<T> of(PropertyInfo<T> property) {
        ArgumentNullException.throwIfNull(property, "property");
        return new LambdaRule<>(property);
    }

    /**
     * 设置验证规则的逻辑函数。
     * <p>
     * 该方法接受一个 BiFunction，允许在验证过程中访问被验证的属性值和规则上下文。函数应返回一个布尔值，指示验证是否通过。
     *
     * @param function 一个 BiFunction，接受属性值和规则上下文，返回布尔值
     * @return 当前 LambdaRule 实例，以便进行链式调用
     */
    public LambdaRule<T> check(BiFunction<T, RuleContext, Boolean> function) {
        ArgumentNullException.throwIfNull(function, "function");
        this.function = function;
        return this;
    }

    /**
     * 设置验证失败时的错误消息生成器。
     * <p>
     * 该方法接受一个 lambda 函数，该函数根据被验证的属性值生成错误消息。这样可以根据不同的属性值动态生成适当的错误消息。
     *
     * @param messageFactory 一个函数，接受属性值并返回错误消息字符串
     * @return 当前 LambdaRule 实例，以便进行链式调用
     */
    public LambdaRule<T> message(Function<T, String> messageFactory) {
        ArgumentNullException.throwIfNull(messageFactory, "messageFactory");
        this.messageFactory = messageFactory;
        return this;
    }

    /**
     * 设置验证失败时的固定错误消息。
     * <p>
     * 该方法接受一个字符串作为错误消息，当验证失败时将使用该消息。如果需要根据属性值动态生成消息，请使用 message(Function<T, String>) 方法。
     *
     * @param message 固定的错误消息字符串
     * @return 当前 LambdaRule 实例，以便进行链式调用
     */
    public LambdaRule<T> message(String message) {
        ArgumentNullException.throwIfNullOrEmpty(message, "message");
        this.messageFactory = (x) -> message;
        return this;
    }
}
