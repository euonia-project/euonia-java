package com.euonia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定字符串必须匹配的正则表达式的注解。可应用于字段或方法参数，表示该字符串必须符合指定的正则表达式模式。
 * <p>
 * 该注解支持以下属性：
 * <ul>
 *     <li>{@code value}：指定正则表达式模式，必须提供。</li>
 *     <li>{@code message}：自定义验证失败时的错误消息，默认为空字符串。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Validation(validator = RegularExpressionValidator.class)
public @interface RegularExpression {

    /**
     * 指定正则表达式模式，必须提供。
     *
     * @return 正则表达式模式
     */
    String value();

    /**
     * 自定义验证失败时的错误消息，默认为空字符串。
     *
     * @return 错误消息
     */
    String message() default "";
}
