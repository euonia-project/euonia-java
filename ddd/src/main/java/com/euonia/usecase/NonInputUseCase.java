package com.euonia.usecase;

import java.util.concurrent.CompletionStage;

/**
 * 定义无输入参数的用例。
 * <p>
 * 对应 .NET 版的 {@code INonInputUseCase<TOutput>}。
 *
 * @param <O> 用例输出的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface NonInputUseCase<O> extends UseCase<EmptyUseCaseInput, O> {

    /**
     * 异步执行用例。
     *
     * @return 用例输出
     */
    CompletionStage<O> executeAsync();

    @Override
    default CompletionStage<O> executeAsync(EmptyUseCaseInput input) {
        return executeAsync();
    }
}
