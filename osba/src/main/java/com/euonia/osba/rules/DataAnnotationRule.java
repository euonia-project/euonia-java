package com.euonia.osba.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import com.euonia.annotation.Validator;
import com.euonia.core.ArgumentNullException;
import com.euonia.osba.BusinessObject;
import com.euonia.reflection.PropertyInfo;
import com.euonia.utility.Assert;

/**
 * 表示一个基于应用于属性的特定注解的规则。
 * 此类的目的是对其关联的属性值执行注解定义的验证逻辑。
 * 该规则检查目标对象是否为业务对象，检索属性的值，然后使用注解指定的验证器来验证该值。
 * 如果验证失败，则将带有适当消息的错误结果添加到上下文中。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DataAnnotationRule<A extends Annotation> extends RuleBase {

    private final A annotation;
    private final Class<?> validatorType;

    /**
     * 使用指定的属性和注解创建 DataAnnotationRule 类的新实例。
     *
     * @param property      与规则关联的属性
     * @param annotation    定义要应用的验证逻辑的注解
     * @param validatorType 将用于根据注解验证属性值的验证器类
     */
    public DataAnnotationRule(PropertyInfo<?> property, A annotation, Class<?> validatorType) {
        super(property, annotation.annotationType().getSimpleName());
        ArgumentNullException.throwIfNull(annotation, "annotation");
        ArgumentNullException.throwIfNull(validatorType, "validatorType");
        this.annotation = annotation;
        this.validatorType = validatorType;
    }

    /**
     * 执行规则的验证逻辑，检查目标对象是否为业务对象，检索属性值，并使用注解指定的验证器来验证该值。
     * 如果验证失败，则将带有适当消息的错误结果添加到上下文中。
     *
     * @param context 规则上下文，包含有关规则执行环境的信息，如目标对象和属性值。
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(RuleContext context) {
        try {
            if (context.getTarget() instanceof BusinessObject<?> businessObject) {

                var value = businessObject.readProperty(getProperty());

                var field = getProperty().getField();
                if (field != null) {
                    Validator<A> validator = (Validator<A>) validatorType.getDeclaredConstructor().newInstance();
                    Validator.Result validate = validator.validate(annotation, value);
                    if (!validate.result()) {
                        context.addErrorResult(validate.message());
                    }
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException
                 | InvocationTargetException exception) {
            context.addErrorResult(exception.getMessage());
        }
    }
}
