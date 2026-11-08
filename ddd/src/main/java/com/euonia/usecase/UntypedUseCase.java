package com.euonia.usecase;

import java.util.concurrent.CompletionStage;

/**
 * 用例的根接口。
 * <p>
 * 对应 .NET 版的 {@code IUseCase}。该接口提供不区分输入/输出类型的统一执行入口，
 * 便于 {@link UseCaseExecutor} 从服务容器解析用例后执行。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface UntypedUseCase {

    /**
     * 以未类型化的方式执行用例。
     *
     * @param input 用例输入
     * @return 用例执行结果
     */
    CompletionStage<?> executeUntypedAsync(Object input);
}
