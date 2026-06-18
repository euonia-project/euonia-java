package com.euonia.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * PipelineBase 是 Pipeline 接口的抽象实现，提供了构建和执行组件管道的基本功能。
 * 它维护一个组件列表，这些组件定义了管道中处理请求的行为。
 * 组件可以通过 use 方法添加到管道中，并且可以按特定顺序或根据其类型添加。
 * build 方法构建管道，将组件组合成一个 PipelineDelegate 实例，
 * runAsync 方法使用给定的请求异步运行管道，并可选地使用累积函数来合并多个组件的结果。
 *
 * @param <C> 请求/上下文对象的类型
 * @param <R> 响应对象的类型
 * @author damon(zhaorong@outlook.com)
 */
public abstract class PipelineBase<C, R> implements Pipeline<C, R> {
    private final List<Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>>> components = new ArrayList<>();

    /**
     * 获取管道中组件的列表。返回一个不可修改的列表。
     *
     * @return 管道中组件的列表
     */
    public List<Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>>> getComponents() {
        return List.copyOf(components);
    }

    /**
     * 向管道添加组件。该组件定义为一个函数，
     * 接收 PipelineDelegate 并返回 PipelineDelegate。
     *
     * @param component 要添加到管道的组件
     * @return 当前管道实例
     */
    @Override
    public Pipeline<C, R> use(Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>> component) {
        components.add(component);
        return this;
    }

    /**
     * 在指定索引位置向管道添加组件。该组件定义为一个函数，
     * 接收 PipelineDelegate 并返回 PipelineDelegate。
     *
     * @param component 要添加到管道的组件
     * @param index     添加组件的索引位置
     * @return 当前管道实例
     */
    @Override
    public Pipeline<C, R> use(Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>> component, int index) {
        components.add(index, component);
        return this;
    }

    /**
     * 向管道添加组件。该组件定义为一个 BiFunction，
     * 接收请求对象和 PipelineDelegate，并返回响应的 CompletionStage。
     *
     * @param handler 要添加到管道的处理器
     * @return 当前管道实例
     */
    @Override
    public Pipeline<C, R> use(BiFunction<C, PipelineDelegate<C, R>, CompletionStage<R>> handler) {
        return use(next -> request -> handler.apply(request, next));
    }

    /**
     * 根据类型向管道添加组件。组件定义为一个类和可选的参数。
     *
     * @param type 要添加的组件的类类型
     * @param args 组件构造函数的可选参数
     * @return 当前管道实例
     */
    @Override
    public Pipeline<C, R> use(Class<?> type, Object... args) {
        return use(next -> getNext(next, type, args));
    }

    /**
     * 根据上下文类型向管道添加组件。组件可以添加在其他组件之前。
     *
     * @param contextType      组件的上下文类型
     * @param useAheadOfOthers 是否将组件添加在其他组件之前
     * @return 当前管道实例
     */
    @Override
    public Pipeline<C, R> useOf(Class<?> contextType, boolean useAheadOfOthers) {
        PipelineBehaviors annotation = contextType.getAnnotation(PipelineBehaviors.class);
        if (annotation == null) {
            return this;
        }
        if (useAheadOfOthers) {
            Class<?>[] behaviorTypes = annotation.value();
            for (int index = 0; index < behaviorTypes.length; index++) {
                Class<?> behaviorType = behaviorTypes[index];
                use(next -> getNext(next, behaviorType), index);
            }
            return this;
        }

        for (Class<?> behaviorType : annotation.value()) {
            use(behaviorType);
        }
        return this;
    }

    /**
     * 构建管道并返回 PipelineDelegate。
     *
     * @return 构建好的 PipelineDelegate
     */
    @Override
    public PipelineDelegate<C, R> build() {
        try {
            PipelineDelegate<C, R> app = request -> CompletableFuture.completedFuture((R) null);
            List<Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>>> reversed = new ArrayList<>(components);
            Collections.reverse(reversed);
            for (Function<PipelineDelegate<C, R>, PipelineDelegate<C, R>> component : reversed) {
                app = component.apply(app);
            }
            return app;
        } finally {
            components.clear();
        }
    }

    /**
     * 使用给定的请求异步运行管道。
     *
     * @param request 要通过管道传递的请求对象
     * @return 表示异步执行结果的 CompletionStage
     */
    @Override
    public CompletionStage<R> runAsync(C request) {
        useOf(request.getClass(), true);
        return build().invoke(request);
    }

    /**
     * 使用给定的请求和累积函数异步运行管道。累积函数用于合并多个组件的结果。
     *
     * @param request    要通过管道传递的请求对象
     * @param accumulate 用于合并多个组件结果的函数
     * @return 表示异步执行结果的 CompletionStage
     */
    @Override
    public CompletionStage<R> runAsync(C request, Function<C, CompletionStage<R>> accumulate) {
        use((req, next) -> accumulate.apply(req));
        return runAsync(request);
    }

    /**
     * 获取下一个 PipelineDelegate 实例，基于指定的类型和构造函数参数创建组件实例。
     *
     * @param next                 下一个 PipelineDelegate 实例
     * @param type                 组件的类类型
     * @param constructorArguments 组件构造函数的参数
     * @return 包含新组件的 PipelineDelegate 实例
     */
    protected abstract PipelineDelegate<C, R> getNext(PipelineDelegate<C, R> next, Class<?> type,
            Object... constructorArguments);
}
