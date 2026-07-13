package com.euonia.http;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 请求上下文感知的执行器工厂，提供能在异步线程中自动传递 {@link RequestContext} 的 {@link Executor}。
 * <p>
 * 提供两种工厂方法：
 * <ul>
 *   <li>{@link #fromCommonPool()} —— 基于 {@link ForkJoinPool#commonPool()} 的执行器</li>
 *   <li>{@link #fromExecutor()} —— 基于固定大小线程池的执行器</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RequestContextAwareExecutor {

    /**
     * 基于 {@link ForkJoinPool#commonPool()} 创建请求上下文感知的执行器。
     *
     * @return 请求上下文感知的执行器
     */
    public static Executor fromCommonPool() {
        var delegate = ForkJoinPool.commonPool();

        return (Runnable command) -> {
            var context = RequestContextAccessor.get();
            delegate.execute(new TaskWithRequestContext(command, context));

            // Runnable decoratedCommand = () -> {
            // try {
            // RequestContextHolder.set(context);
            // command.run();
            // } finally {
            // RequestContextHolder.remove();
            // }
            // };
            // new Thread(decoratedCommand).start();
        };
    }

    /**
     * 基于固定大小（4 线程）线程池创建请求上下文感知的执行器，提交的任务会自动包装请求上下文。
     *
     * @return 请求上下文感知的执行器
     */
    public static Executor fromExecutor() {
        return new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000)) {
            @SuppressWarnings("NullableProblems")
            @Override
            public void execute(Runnable command) {
                // 拦截提交的任务，进行上下文包装
                super.execute(new RequestContextCopyingDecorator().decorate(command));
            }
        };
    }

    /**
     * 带有请求上下文的可运行任务，执行时自动设置并恢复请求上下文。
     */
    private record TaskWithRequestContext(Runnable command, RequestContext context) implements Runnable {

        @Override
        public void run() {
            var previous = RequestContextAccessor.get();
            try {
                RequestContextAccessor.set(context);
                command.run();
            } finally {
                if (previous != null) {
                    RequestContextAccessor.set(previous);
                } else {
                    RequestContextAccessor.remove();
                }
            }
        }
    }
}
