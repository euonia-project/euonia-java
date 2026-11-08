package com.euonia.usecase;

import java.util.concurrent.CompletionStage;

/**
 * 定义无业务输出的用例。
 * <p>
 * 对应 .NET 版的 {@code INonOutputUseCase<TInput>}。执行成功时统一适配为
 * {@link EmptyUseCaseOutput#INSTANCE}。
 *
 * @param <I> 用例输入的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface NonOutputUseCase<I> extends UseCase<I, EmptyUseCaseOutput> {

    /**
     * 使用给定的输入异步执行用例。
     *
     * @param input 用例的输入数据
     * @return 表示执行完成的 {@link CompletionStage}
     */
    CompletionStage<Void> executeVoidAsync(I input);

    @Override
    default CompletionStage<EmptyUseCaseOutput> executeAsync(I input) {
        return executeVoidAsync(input).thenApply(ignored -> EmptyUseCaseOutput.INSTANCE);
    }
}
