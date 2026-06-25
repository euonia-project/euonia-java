package com.euonia.http;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestContextAwareExecutor {

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
