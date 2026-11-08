package com.euonia.usecase;

import java.util.concurrent.CompletionStage;

/**
 * 定义既无输入参数也无业务输出的用例。
 * <p>
 * 对应 .NET 版的 {@code IParameterlessUseCase}。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface ParameterlessUseCase extends UseCase<EmptyUseCaseInput, EmptyUseCaseOutput> {

    /**
     * 异步执行用例。
     *
     * @return 表示执行完成的 {@link CompletionStage}
     */
    CompletionStage<Void> executeVoidAsync();

    @Override
    default CompletionStage<EmptyUseCaseOutput> executeAsync(EmptyUseCaseInput input) {
        return executeVoidAsync().thenApply(ignored -> EmptyUseCaseOutput.INSTANCE);
    }
}
