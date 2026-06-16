package com.euonia.factory.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FactoryExecute 注解用于标记一个方法，表示该方法是一个工厂方法，用于执行特定操作。
 * 被标记的方法通常会被工厂框架调用，以执行特定类型的操作。
 *
 * @author damon(zhaorong@outlook)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryExecute {
}
