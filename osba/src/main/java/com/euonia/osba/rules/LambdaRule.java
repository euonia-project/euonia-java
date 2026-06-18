package com.euonia.osba.rules;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    private final BiFunction<T, RuleContext, Boolean> function;
    private final Function<T, String> messageFactory;

    public LambdaRule(PropertyInfo<T> property, BiFunction<T, RuleContext, Boolean> function, String message) {
        this(property, function, x -> message);
    }

    public LambdaRule(PropertyInfo<T> property, BiFunction<T, RuleContext, Boolean> function, Function<T, String> messageFactory) {
        super(property);
        this.property = property;
        this.function = function;
        this.messageFactory = messageFactory;
    }

    /**
     * 执行规则的验证逻辑，检查目标对象是否为业务对象，检索属性值，并使用提供的 lambda 函数验证该值。
     * 如果验证失败，则将带有适当消息的错误结果添加到上下文中。
     *
     * @param context 规则上下文，包含有关规则执行环境的信息，如目标对象和属性值。
     * @return 一个表示异步操作的 CompletableFuture 对象
     */
    @Override
    public CompletableFuture<Void> executeAsync(RuleContext context) {
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
        return CompletableFuture.completedFuture(null);
    }
}
