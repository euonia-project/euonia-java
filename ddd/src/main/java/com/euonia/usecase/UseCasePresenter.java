package com.euonia.usecase;

import java.util.function.Consumer;

/**
 * 定义用例执行结果的呈现器。
 * <p>
 * 对应 .NET 版的 {@code IUseCasePresenter<TOutput>}。调用方通过
 * {@link #onSucceed(Consumer)}、{@link #onFailed(Consumer)} 与
 * {@link #onCanceled(Runnable)} 订阅结果，无需自行编写 try/catch 分发逻辑。
 *
 * @param <O> 用例执行结果（输出）的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCasePresenter<O> extends UseCaseOutputSuccess<O>, UseCaseOutputFailure, AutoCloseable {

    /**
     * 注册用例成功执行时的监听器。
     *
     * @param listener 成功结果监听器
     * @return 当前呈现器，便于链式调用
     */
    UseCasePresenter<O> onSucceed(Consumer<O> listener);

    /**
     * 注册用例执行失败时的监听器。
     *
     * @param listener 失败结果监听器
     * @return 当前呈现器，便于链式调用
     */
    UseCasePresenter<O> onFailed(Consumer<Throwable> listener);

    /**
     * 注册用例被取消时的监听器。
     *
     * @param listener 取消监听器
     * @return 当前呈现器，便于链式调用
     */
    UseCasePresenter<O> onCanceled(Runnable listener);

    /**
     * 获取用例执行成功时的输出。
     *
     * @return 用例输出；尚未成功执行时为 {@code null}
     */
    O getOutput();

    /**
     * 同时订阅成功与失败结果。
     *
     * @param onSuccess 成功结果监听器
     * @param onFailure 失败结果监听器
     */
    default void subscribe(Consumer<O> onSuccess, Consumer<Throwable> onFailure) {
        onSucceed(onSuccess);
        onFailed(onFailure);
    }

    /**
     * 释放呈现器持有的监听器。
     */
    @Override
    void close();
}
