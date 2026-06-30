package com.euonia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定数值范围的注解。可应用于字段或方法参数，表示该数值必须在指定的最小值和最大值之间。
 * <p>
 * 该注解支持以下属性：
 * <ul>
 *     <li>{@code min}：指定允许的最小值，默认为 {@code Long.MIN_VALUE}。</li>
 *     <li>{@code max}：指定允许的最大值，默认为 {@code Long.MAX_VALUE}。</li>
 *     <li>{@code inclusiveMin}：是否包含最小值，默认为 {@code false}。</li>
 *     <li>{@code inclusiveMax}：是否包含最大值，默认为 {@code false}。</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Validation(validator = RangeValidator.class)
public @interface Range {
    /**
     * 指定允许的最小值。
     *
     * @return 最小值，默认为 {@code Long.MIN_VALUE}
     */
    long min() default Long.MIN_VALUE;

    /**
     * 指定允许的最大值。
     *
     * @return 最大值，默认为 {@code Long.MAX_VALUE}
     */
    long max() default Long.MAX_VALUE;

    /**
     * 是否包含最小值。为{@code true}时允许指定值等于最小值。
     *
     * @return 是否包含最小值，默认为 {@code false}
     */
    boolean inclusiveMin() default false;

    /**
     * 是否包含最大值。为{@code true}时允许指定值等于最大值。
     *
     * @return 是否包含最大值，默认为 {@code false}
     */
    boolean inclusiveMax() default false;
}
