package com.euonia.usecase;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * 用例呈现器，实现了用例的输出端口，允许订阅者监听成功和失败事件。
 * <p>
 * 同时实现了 {@link UseCaseSuccess}、{@link UseCaseFailure} 和 {@link AutoCloseable} 接口，
 * 基于 {@link SubmissionPublisher} 实现响应式的事件传递。
 *
 * @param <O> 用例成功输出的类型
 * @author damon(zhaorong@outlook.com)
 */
public class UseCasePresenter<O> implements UseCaseSuccess<O>, UseCaseFailure, AutoCloseable {
    /**
     * 事件发布者，用于向订阅者推送成功/失败事件
     */
    private final Publisher<O> publisher = new SubmissionPublisher<>();

    /**
     * 用例执行的成功输出
     */
    private O output;

    /**
     * 订阅呈现器以接收成功和失败事件。
     *
     * @param onSuccess 用于处理成功输出的消费者
     * @param onFailure 用于处理错误的消费者
     */
    public void subscribe(Consumer<O> onSuccess, Consumer<Throwable> onFailure) {
        publisher.subscribe(new Subscriber<>() {

            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(O item) {
                onSuccess.accept(item);
            }

            @Override
            public void onError(Throwable throwable) {
                onFailure.accept(throwable);
            }

            @Override
            public void onComplete() {
                // 如有需要，处理完成事件
            }
        });
    }

    @Override
    public void success(O output) {
        this.output = output;
        ((SubmissionPublisher<O>) publisher).submit(output);
    }

    @Override
    public void error(Throwable throwable) {
        ((SubmissionPublisher<O>) publisher).closeExceptionally(throwable);
    }

    @Override
    public void close() {
        ((SubmissionPublisher<O>) publisher).close();
    }

    /**
     * 返回用例执行成功时的输出。
     *
     * @return 用例执行的输出
     */
    public O getOutput() {
        return output;
    }
}
