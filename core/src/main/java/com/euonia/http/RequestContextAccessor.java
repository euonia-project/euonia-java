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

    /**
     * 设置当前请求上下文。
     *
     * @param context 当前请求上下文
     */
    void setContext(RequestContext context);

    /**
     * 移除当前请求上下文。
     */
    void removeContext();

    static RequestContext get() {
        return DefaultRequestContextAccessor.get();
    }

    static void set(RequestContext context) {
        DefaultRequestContextAccessor.set(context);
    }

    static void remove() {
        DefaultRequestContextAccessor.remove();
    }
}
