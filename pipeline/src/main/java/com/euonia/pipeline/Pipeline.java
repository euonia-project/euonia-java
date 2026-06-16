package com.euonia.pipeline;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Pipeline 接口定义了构建和执行组件管道的契约，这些组件可以处理请求并生成响应。
 * 它允许向管道添加组件、构建管道以及使用给定的请求异步运行管道。
 * 组件可以按特定顺序或根据其类型添加，管道可以使用可选的累积函数来执行，以合并多个组件的结果。
 *
 * @param <C> 请求/上下文对象的类型
 * @param <R> 响应对象的类型
 * @author damon(zhaorong@outlook)
 */
public interface Pipeline<C, R> {
    /**
     * 向管道添加组件。该组件定义为一个函数，
     * 接收 PipelineDelegate 并返回 PipelineDelegate。
     *
     * @param component 要添加到管道的组件
     * @return 当前管道实例
     */
    Pipeline<C, R> use(Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>> component);

    /**
     * 在指定索引位置向管道添加组件。该组件定义为一个函数，
     * 接收 PipelineDelegate 并返回 PipelineDelegate。
     *
     * @param component 要添加到管道的组件
     * @param index     添加组件的索引位置
     * @return 当前管道实例
     */
    Pipeline<C, R> use(Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>> component, int index);

    /**
     * 向管道添加组件。该组件定义为一个 BiFunction，
     * 接收请求对象和 PipelineDelegate，并返回响应的 CompletionStage。
     *
     * @param handler 要添加到管道的处理器
     * @return 当前管道实例
     */
    Pipeline<C, R> use(BiFunction<C, PipelineDelegate<C, R>, CompletionStage<R>> handler);

    /**
     * 根据类型向管道添加组件。组件定义为一个类和可选的参数。
     *
     * @param type 要添加的组件的类类型
     * @param args 组件构造函数的可选参数
     * @return 当前管道实例
     */
    Pipeline<C, R> use(Class<?> type, Object... args);

    /**
     * 根据上下文类型向管道添加组件。组件可以添加在其他组件之前。
     *
     * @param contextType      组件的上下文类型
     * @param useAheadOfOthers 是否将组件添加在其他组件之前
     * @return 当前管道实例
     */
    Pipeline<C, R> useOf(Class<?> contextType, boolean useAheadOfOthers);

    /**
     * 构建管道并返回 PipelineDelegate。
     *
     * @return 构建好的 PipelineDelegate
     */
    PipelineDelegate<C, R> build();

    /**
     * 使用给定的请求异步运行管道。
     *
     * @param request 要通过管道传递的请求对象
     * @return 表示异步执行结果的 CompletionStage
     */
    CompletionStage<R> runAsync(C request);

    /**
     * 使用给定的请求和累积函数异步运行管道。
     *
     * @param request    要通过管道传递的请求对象
     * @param accumulate 用于累积多个组件结果的函数
     * @return 表示异步执行结果的 CompletionStage
     */
    CompletionStage<R> runAsync(C request, Function<C, CompletionStage<R>> accumulate);
}
