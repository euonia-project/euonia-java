package com.euonia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定字符串长度范围的注解。可应用于字段或方法参数，表示该字符串的长度必须在指定的最小值和最大值之间。
 * <p>
 * 该注解支持以下属性：
 * <ul>
 *     <li>{@code min}：指定允许的最小长度，默认为 {@code 0}。</li>
 *     <li>{@code max}：指定允许的最大长度，默认为 {@code Integer.MAX_VALUE}。</li>
 *     <li>{@code message}：自定义验证失败时的错误消息，默认为空字符串。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Validation(validator = StringLengthValidator.class)
public @interface StringLength {
    /**
     * 指定允许的最小长度。
     *
     * @return 最小长度, 默认为 {@code 0}
     */
    int min() default 0;

    /**
     * 指定允许的最大长度。
     *
     * @return 最大长度, 默认为 {@code Integer.MAX_VALUE}
     */
    int max() default Integer.MAX_VALUE;

    /**
     * 自定义验证失败时的错误消息。默认为空字符串。
     *
     * @return 错误消息, 默认为空字符串
     */
    String message() default "";
}
