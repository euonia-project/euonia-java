package com.euonia.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记消息为多播消息，表示消息将发送给多个订阅者。
 * 所有订阅了该消息类型的订阅者都会收到该消息。
 * <p>
 * 多播消息通常用于需要广播到多个订阅者且不期望响应的事件或通知。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Multicast {
}
