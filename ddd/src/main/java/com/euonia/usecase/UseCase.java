package com.euonia.usecase;

import java.util.concurrent.CompletionStage;

/**
 * 定义具有输入与输出的用例。
 * <p>
 * 对应 .NET 版的 {@code IUseCase<TInput, TOutput>}。Java 版使用
 * {@link CompletionStage} 表示异步执行结果。
 *
 * @param <I> 用例输入的类型
 * @param <O> 用例输出的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCase<I, O> extends UntypedUseCase {

    /**
     * 使用给定的输入异步执行用例。
     *
     * @param input 用例的输入数据
     * @return 用例输出
     */
    CompletionStage<O> executeAsync(I input);

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException 当 {@code input} 无法转换为 {@code I} 时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    default CompletionStage<?> executeUntypedAsync(Object input) {
        return executeAsync((I) input);
    }
}
