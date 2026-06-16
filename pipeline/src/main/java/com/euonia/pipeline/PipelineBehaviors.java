package com.euonia.pipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PipelineBehaviors 注解用于标记一个类，指定与该类相关的管道行为组件。
 * 该注解包含一个 value 属性，它是一个 Class<?> 数组，表示与该类相关的管道行为组件的类型。
 *
 * @author damon(zhaorong@outlook)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PipelineBehaviors {
    Class<?>[] value();
}
