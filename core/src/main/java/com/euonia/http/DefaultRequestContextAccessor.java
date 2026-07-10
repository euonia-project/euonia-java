package com.euonia.http;

/**
 * {@link RequestContextAccessor} 的默认实现，基于 {@link ThreadLocal} 存储请求上下文。
 * <p>
 * 采用单例模式，每个线程拥有独立的上下文副本。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class DefaultRequestContextAccessor implements RequestContextAccessor {
    /** 线程本地存储 */
    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    /** 单例实例 */
    private static final DefaultRequestContextAccessor INSTANCE = new DefaultRequestContextAccessor();

    private DefaultRequestContextAccessor() {
    }

    /**
     * 获取单例实例。
     *
     * @return 单例实例
     */
    public static DefaultRequestContextAccessor getInstance() {
        return INSTANCE;
    }

    /**
     * 设置当前线程的请求上下文。
     *
     * @param context 要设置的请求上下文
     */
    @Override
    public void setContext(RequestContext context) {
        HOLDER.set(context);
    }

    /**
     * 移除当前线程的请求上下文。
     */
    @Override
    public void removeContext() {
        HOLDER.remove();
    }

    /**
     * 获取当前线程的请求上下文。
     *
     * @return 当前请求上下文
     */
    @Override
    public RequestContext getContext() {
        return HOLDER.get();
    }

    /**
     * 获取当前线程的请求上下文的静态便捷方法。
     *
     * @return 当前请求上下文
     */
    public static RequestContext get() {
        return getInstance().getContext();
    }

    /**
     * 设置当前线程的请求上下文的静态便捷方法。
     *
     * @param context 要设置的请求上下文
     */
    public static void set(RequestContext context) {
        getInstance().setContext(context);
    }

    /**
     * 移除当前线程的请求上下文的静态便捷方法。
     */
    public static void remove() {
        getInstance().removeContext();
    }
}
