package com.euonia.http;

/**
 * 请求上下文复制装饰器，将当前线程的请求上下文复制到新线程中执行。
 * <p>
 * 在异步任务切换线程时，确保 {@link RequestContext} 能跨线程传递。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RequestContextCopyingDecorator {

    /**
     * 装饰一个 {@link Runnable}，使其在执行时自动携带当前请求上下文。
     *
     * @param runnable 原始任务
     * @return 装饰后的任务，携带请求上下文
     */
    public Runnable decorate(Runnable runnable) {
        var context = RequestContextAccessor.get();

        return () -> {
            var previous = RequestContextAccessor.get();
            try {
                RequestContextAccessor.set(context);
                runnable.run();
            } finally {
                if (previous != null) {
                    RequestContextAccessor.set(previous);
                } else {
                    RequestContextAccessor.remove();
                }
            }
        };
    }
}
