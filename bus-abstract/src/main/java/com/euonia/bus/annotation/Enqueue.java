package com.euonia.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记消息应入队等待处理。{@code value} 属性指定了队列名称，
 * {@code priority} 属性可用于指示消息的处理优先级，值越高优先级越高。
 * <p>
 * 此注解通常与消息总线或队列系统配合使用，以确保消息按正确顺序和适当的紧急程度进行处理。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Enqueue {
    /**
     * 队列名称。
     *
     * @return 队列名称
     */
    String value();

    /**
     * 消息的处理优先级，值越高优先级越高，默认值为 0。
     *
     * @return 消息的优先级
     */
    int priority() default 0;
}
