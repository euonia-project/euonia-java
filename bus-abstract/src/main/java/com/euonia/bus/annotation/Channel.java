package com.euonia.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定消息处理器订阅的通道名称。
 * <p>
 * 可应用于类型或参数级别，在运行时保留以便消息总线框架通过反射获取。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface Channel {
    /**
     * 通道名称。
     *
     * @return 通道名称
     */
    String value();
}
