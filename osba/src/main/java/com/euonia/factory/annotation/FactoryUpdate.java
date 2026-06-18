package com.euonia.factory.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FactoryUpdate 注解用于标记一个方法，表示该方法是一个工厂方法，用于更新对象实例。
 * 被标记的方法通常会被工厂框架调用，以更新特定类型的对象。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryUpdate {
}
