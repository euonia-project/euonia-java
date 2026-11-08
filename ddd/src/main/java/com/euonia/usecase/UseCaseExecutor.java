package com.euonia.usecase;

import java.util.concurrent.CompletionStage;

/**
 * 定义统一执行用例并把结果分发到 {@link UseCasePresenter} 的契约。
 * <p>
 * 对应 .NET 版的 {@code IUseCaseExecutor}：用例成功时调用
 * {@code presenter.ok(output)}，执行抛出异常时调用
 * {@code presenter.error(throwable)}，调用方无需自行编写 try/catch 分发逻辑。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCaseExecutor {

    /**
     * 执行带输入与输出的用例。
     *
     * @param <I>       用例输入类型
     * @param <O>       用例输出类型
     * @param useCase   要执行的用例
     * @param input     用例输入
     * @param presenter 接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <I, O> CompletionStage<Void> executeAsync(UseCase<I, O> useCase, I input, UseCasePresenter<O> presenter);

    /**
     * 执行只接收输入、无业务输出的用例。
     *
     * @param <I>       用例输入类型
     * @param useCase   要执行的用例
     * @param input     用例输入
     * @param presenter 接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <I> CompletionStage<Void> executeAsync(NonOutputUseCase<I> useCase, I input, UseCasePresenter<EmptyUseCaseOutput> presenter);

    /**
     * 执行只产生输出、无输入的用例。
     *
     * @param <O>       用例输出类型
     * @param useCase   要执行的用例
     * @param presenter 接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <O> CompletionStage<Void> executeAsync(NonInputUseCase<O> useCase, UseCasePresenter<O> presenter);

    /**
     * 执行既无输入也无业务输出的用例。
     *
     * @param useCase   要执行的用例
     * @param presenter 接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    CompletionStage<Void> executeAsync(ParameterlessUseCase useCase, UseCasePresenter<EmptyUseCaseOutput> presenter);

    /**
     * 从服务容器解析带输入与输出的用例并执行。
     *
     * @param <C>          用例类型
     * @param <O>          用例输出类型
     * @param useCaseType  用例类型
     * @param input        用例输入
     * @param presenter    接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <C extends UseCase<?, O>, O> CompletionStage<Void> executeAsync(Class<C> useCaseType, Object input, UseCasePresenter<O> presenter);

    /**
     * 从服务容器解析只接收输入、无业务输出的用例并执行。
     *
     * @param <C>         用例类型
     * @param useCaseType 用例类型
     * @param input       用例输入
     * @param presenter   接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <C extends NonOutputUseCase<?>> CompletionStage<Void> executeNonOutputAsync(Class<C> useCaseType, Object input, UseCasePresenter<EmptyUseCaseOutput> presenter);

    /**
     * 从服务容器解析只产生输出、无输入的用例并执行。
     *
     * @param <C>         用例类型
     * @param <O>         用例输出类型
     * @param useCaseType 用例类型
     * @param presenter   接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <C extends NonInputUseCase<O>, O> CompletionStage<Void> executeNonInputAsync(Class<C> useCaseType, UseCasePresenter<O> presenter);

    /**
     * 从服务容器解析既无输入也无业务输出的用例并执行。
     *
     * @param <C>         用例类型
     * @param useCaseType 用例类型
     * @param presenter   接收执行结果的呈现器
     * @return 表示执行完成的 {@link CompletionStage}
     */
    <C extends ParameterlessUseCase> CompletionStage<Void> executeParameterlessAsync(Class<C> useCaseType, UseCasePresenter<EmptyUseCaseOutput> presenter);
}
