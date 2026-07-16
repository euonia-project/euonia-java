package com.euonia.http;

/**
 * 定义请求上下文访问器的接口。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RequestContextAccessor {
    /**
     * 线程本地存储
     */
    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    /**
     * 私有构造函数，防止实例化。
     */
    private RequestContextAccessor() {
    }

    /**
     * 获取当前请求上下文。
     *
     * @return 当前请求上下文
     */
    public static RequestContext get() {
        return HOLDER.get();
    }

    /**
     * 设置当前请求上下文。
     *
     * @param context 当前请求上下文
     */
    public static void set(RequestContext context) {
        HOLDER.set(context);
    }

    /**
     * 移除当前请求上下文。
     */
    public static void remove() {
        HOLDER.remove();
    }
}
