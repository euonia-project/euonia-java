package com.euonia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定验证器的注解。该注解可应用于其他注解类型上，用于关联一个验证器类。
 * <p>
 * 该注解支持以下属性：
 * <ul>
 *     <li>{@code validator}：指定验证器类，该类必须实现 {@link Validator} 接口。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Validation {
    /**
     * 指定验证器类，该类必须实现 {@link Validator} 接口。
     *
     * @return 验证器类
     */
    Class<? extends Validator<?>> validator();
}
