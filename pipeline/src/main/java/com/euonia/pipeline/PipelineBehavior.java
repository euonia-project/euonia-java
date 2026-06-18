package com.euonia.pipeline;

import java.util.concurrent.CompletionStage;

/**
 * PipelineBehavior 定义了管道行为的接口，表示在管道中处理请求的组件。
 * 它包含一个 handleAsync 方法，该方法接收请求对象和 PipelineDelegate，
 * 并返回一个表示异步执行结果的 CompletionStage。
 *
 * @param <C> 请求/上下文对象的类型
 * @param <R> 响应对象的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface PipelineBehavior<C, R> {
    /**
     * 异步处理请求的方法。
     *
     * @param context 请求/上下文对象
     * @param next    下一个管道委托
     * @return 表示异步执行结果的 CompletionStage
     */
    CompletionStage<R> handleAsync(C context, PipelineDelegate<C, R> next);
}
