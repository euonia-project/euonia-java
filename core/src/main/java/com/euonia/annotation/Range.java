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
 *     <li>{@code min}：指定允许的最小值，默认为 {@code Double.MIN_VALUE}。</li>
 *     <li>{@code max}：指定允许的最大值，默认为 {@code Double.MAX_VALUE}。</li>
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
     * 指定值范围的区间表达式，例如[1,10],(1,10),(1,10],[9,)等。
     * <p>
     * 如果指定了区间表达式则忽略 min、max、minBoundary 和 maxBoundary 属性。
     *
     * @return 区间表达式
     */
    String value() default "";

    /**
     * 指定允许的最小值。
     *
     * @return 最小值，默认为 {@code Double.MIN_VALUE}
     */
    double min() default Double.MIN_VALUE;

    /**
     * 指定允许的最大值。
     *
     * @return 最大值，默认为 {@code Double.MAX_VALUE}
     */
    double max() default Double.MAX_VALUE;

    /**
     * 指定最小值的边界类型。
     *
     * @return 最小值的边界类型，默认为 {@code EXCLUSIVE}
     */
    Boundary minBoundary() default Boundary.EXCLUSIVE;

    /**
     * 指定最大值的边界类型。
     *
     * @return 最大值的边界类型，默认为 {@code EXCLUSIVE}
     */
    Boundary maxBoundary() default Boundary.EXCLUSIVE;

    /**
     * 自定义验证失败时的错误消息。默认为空字符串。
     *
     * @return 错误消息，默认为空字符串
     */
    String message() default "";

    /**
     * 枚举类型，表示范围的边界类型。包括：
     * <ul>
     *     <li>{@code INCLUSIVE}：包含边界值。</li>
     *     <li>{@code EXCLUSIVE}：不包含边界值。</li>
     * </ul>
     */
    enum Boundary {
        /**
         * 包含边界值。
         */
        INCLUSIVE,
        /**
         * 不包含边界值。
         */
        EXCLUSIVE
    }
}
