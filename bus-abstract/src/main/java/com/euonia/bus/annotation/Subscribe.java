package com.euonia.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法为消息订阅处理器，指定要订阅的通道和分组名称。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscribe {
    /**
     * 要订阅的通道名称。
     *
     * @return 通道名称
     */
    String value() default "";

    /**
     * 此订阅的分组名称。
     * 发布到同一通道但分组名称不同的消息将投递给不同的订阅者，
     * 而发布到同一通道和分组名称的消息将在同组订阅者之间负载均衡。
     *
     * @return 此订阅的分组名称
     */
    String group() default "";
}
