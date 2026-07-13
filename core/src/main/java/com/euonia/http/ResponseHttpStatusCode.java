package com.euonia.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注类对应的 HTTP 响应状态码。
 * 用于在异常类上声明其对应的 HTTP 状态码（如 400、404 等），
 * 以便框架自动映射。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseHttpStatusCode {
    /**
     * 与此响应关联的 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    int value();
}
