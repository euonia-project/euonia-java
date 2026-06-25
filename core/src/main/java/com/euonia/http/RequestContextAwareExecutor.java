package com.euonia.http;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class RequestContextAwareExecutor implements Executor {

    private final Executor delegate;

    public RequestContextAwareExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    public static RequestContextAwareExecutor wrap(Executor delegate) {
        if (delegate instanceof RequestContextAwareExecutor) {
            return (RequestContextAwareExecutor) delegate;
        }
        return new RequestContextAwareExecutor(delegate);
    }

    public static RequestContextAwareExecutor fromCommonPool() {
        return new RequestContextAwareExecutor(ForkJoinPool.commonPool());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void execute(Runnable command) {
        var context = DefaultRequestContextAccessor.get();
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
    }

    private record TaskWithRequestContext(Runnable command, RequestContext context) implements Runnable {

        @Override
        public void run() {
            var previous = DefaultRequestContextAccessor.get();
            try {
                DefaultRequestContextAccessor.set(context);
                command.run();
            } finally {
                if (previous != null) {
                    DefaultRequestContextAccessor.set(previous);
                } else {
                    DefaultRequestContextAccessor.remove();
                }
            }
        }
    }
}
