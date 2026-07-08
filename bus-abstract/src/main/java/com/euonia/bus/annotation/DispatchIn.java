package com.euonia.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定消息应在哪些传输通道上被派发（出站）。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DispatchIn {
    /**
     * 出站传输通道名称数组。
     *
     * @return 传输通道名称
     */
    String[] transports() default {};
}
