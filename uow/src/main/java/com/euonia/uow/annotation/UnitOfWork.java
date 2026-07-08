package com.euonia.uow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记某个类型或方法需要工作单元支持。
 *
 * <p>当应用于类上时，所有方法都会被包装在工作单元中。
 * 当应用于方法上时，仅该特定方法会被包装。
 *
 * <pre>{@code
 * @UnitOfWork
 * public class OrderService { ... }
 *
 * @UnitOfWork(disabled = true)
 * public void readOnlyQuery() { ... }
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 * @see com.euonia.uow.UnitOfWork
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitOfWork {
    /**
     * 当设置为 {@code true} 时，即使父作用域声明了工作单元，
     * 也会为被注解的元素抑制工作单元。
     *
     * @return 工作单元是否被禁用
     */
    boolean disabled() default false;
}
