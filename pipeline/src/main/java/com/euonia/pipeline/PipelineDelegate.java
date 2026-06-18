package com.euonia.pipeline;

import java.util.concurrent.CompletionStage;

/**
 * PipelineDelegate 定义了管道委托的接口，表示在管道中处理请求的组件。
 * 它包含一个 invoke 方法，该方法接收请求对象并返回一个表示异步执行结果的 CompletionStage。
 *
 * @param <C> 请求/上下文对象的类型
 * @param <R> 响应对象的类型
 * @author damon(zhaorong@outlook.com)
 */
@FunctionalInterface
public interface PipelineDelegate<C, R> {
    /**
     * 异步调用管道委托的方法。
     *
     * @param context 请求/上下文对象
     * @return 表示异步执行结果的 CompletionStage
     */
    CompletionStage<R> invoke(C context);
}
