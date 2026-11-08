package com.euonia.usecase;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * {@link UseCasePresenter} 的默认实现。
 * <p>
 * 对应 .NET 版的 {@code DefaultUseCasePresenter<TOutput>}。支持注册多个
 * 成功、失败与取消监听器；{@link #close()} 会清空所有监听器。
 *
 * @param <O> 用例执行结果的类型
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultUseCasePresenter<O> implements UseCasePresenter<O> {

    private final List<Consumer<O>> succeedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Throwable>> failedListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> canceledListeners = new CopyOnWriteArrayList<>();

    private volatile O output;

    @Override
    public UseCasePresenter<O> onSucceed(Consumer<O> listener) {
        if (listener != null) {
            succeedListeners.add(listener);
        }
        return this;
    }

    @Override
    public UseCasePresenter<O> onFailed(Consumer<Throwable> listener) {
        if (listener != null) {
            failedListeners.add(listener);
        }
        return this;
    }

    @Override
    public UseCasePresenter<O> onCanceled(Runnable listener) {
        if (listener != null) {
            canceledListeners.add(listener);
        }
        return this;
    }

    @Override
    public void ok(O output) {
        this.output = output;
        for (var listener : succeedListeners) {
            listener.accept(output);
        }
    }

    @Override
    public void error(Throwable throwable) {
        if (isCancellation(throwable)) {
            for (var listener : canceledListeners) {
                listener.run();
            }
            return;
        }

        for (var listener : failedListeners) {
            listener.accept(throwable);
        }
    }

    @Override
    public O getOutput() {
        return output;
    }

    @Override
    public void close() {
        succeedListeners.clear();
        failedListeners.clear();
        canceledListeners.clear();
    }

    private static boolean isCancellation(Throwable throwable) {
        return throwable instanceof CancellationException
            || throwable instanceof InterruptedException;
    }
}
