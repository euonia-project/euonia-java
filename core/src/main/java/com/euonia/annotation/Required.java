package com.euonia.annotation;

import java.lang.annotation.*;

/**
 * 用于标记字段或方法参数为必填项的注解。被标记的字段或参数在验证时必须非空，除非设置了 {@code allowEmpty} 为 {@code true}。
 * <p>
 * 该注解支持以下属性：
 * <ul>
 *     <li>{@code allowEmpty}：是否允许空字符串，默认为 {@code false}。</li>
 *     <li>{@code message}：自定义验证失败时的错误消息，默认为空字符串。</li>
 *     <li>{@code annotation}：指定注解类型，默认为 {@code Required.class}。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Validation(validator = RequiredValidator.class)
public @interface Required {

    boolean allowEmpty() default false;

    String message() default "";

    Class<? extends Annotation> annotation() default Required.class;
}
