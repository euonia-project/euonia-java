package com.euonia.http;

/**
 * 定义请求上下文访问器的接口。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface RequestContextAccessor {
    /**
     * 获取当前请求上下文。
     *
     * @return 当前请求上下文
     */
    RequestContext getContext();
}
