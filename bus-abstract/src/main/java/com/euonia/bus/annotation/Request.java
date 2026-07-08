package com.euonia.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记消息为请求消息，表示消息将被发送到一个或多个订阅者并期望收到响应。
 * 总线会确保订阅者能够将响应发送回发送者。
 * <p>
 * 响应的类型由 {@code responseType} 属性指定，指示订阅者在收到请求消息后应返回的响应类型。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Request {
    /**
     * 期望的响应类型。
     *
     * @return 响应类型的 {@link Class}
     */
    Class<?> responseType();
}
