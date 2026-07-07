package com.euonia.domain.auditing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解，用于标记需要进行审计跟踪的类、字段或方法。
 * <p>
 * 当应用于类型（TYPE）级别时，表示该类型的所有实例都应被审计；
 * 当应用于字段或方法级别时，仅审计被标记的成员。
 * <p>
 * 注解在运行时保留，以便审计框架通过反射发现和拦截。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface Audited {
}
