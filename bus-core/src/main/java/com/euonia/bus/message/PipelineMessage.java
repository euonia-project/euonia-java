package com.euonia.bus.message;

import java.util.concurrent.CompletableFuture;

import com.euonia.pipeline.Pipeline;

/**
 * 管道消息封装类，将消息与 {@link Pipeline} 关联，支持链式调用管道步骤并异步执行。
 *
 * @param <M> 消息类型
 * @param <R> 管道执行的响应类型
 * @author damon(zhaorong@outlook)
 */
public class PipelineMessage<M, R> {
    /** 原始消息对象 */
    private final M message;
    /** 关联的管道实例 */
    private Pipeline<M, R> pipeline;

    /**
     * 仅使用消息创建 {@link PipelineMessage} 实例，管道需后续通过 {@link #use(Class, Object...)} 设置。
     *
     * @param message 消息对象
     */
    public PipelineMessage(M message) {
        this.message = message;
    }

    /**
     * 使用消息和管道创建 {@link PipelineMessage} 实例。
     *
     * @param message  消息对象
     * @param pipeline 关联的管道实例
     */
    public PipelineMessage(M message, Pipeline<M, R> pipeline) {
        this(message);
        this.pipeline = pipeline;
    }

    /**
     * 获取原始消息对象。
     *
     * @return 消息对象
     */
    public M getMessage() {
        return message;
    }

    /**
     * 获取关联的管道实例。
     *
     * @return 管道实例，可能为 {@code null}
     */
    public Pipeline<M, R> getPipeline() {
        return pipeline;
    }

    /**
     * 向管道中添加一个步骤类型，并返回当前实例以支持链式调用。
     *
     * @param type 管道步骤的类型
     * @param args 管道步骤的构造参数
     * @return 当前 {@link PipelineMessage} 实例
     */
    public PipelineMessage<M, R> use(Class<?> type, Object... args) {
        pipeline = pipeline.use(type, args);
        return this;
    }

    /**
     * 异步执行管道并将结果包装为 {@link CompletableFuture}。
     *
     * @return 包含管道执行结果的 {@link CompletableFuture}
     */
    public CompletableFuture<R> executeAsync() {
        return pipeline.runAsync(message)
                       .toCompletableFuture();
    }

}
