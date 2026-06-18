package com.euonia.pipeline;

/**
 * PipelineFactory 负责创建 Pipeline 的实例。
 * 它抽象了创建逻辑，允许使用不同的 Pipeline 实现，
 * 而无需更改依赖它的客户端代码。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface PipelineFactory {
    /**
     * 创建一个新的 Pipeline 实例，并指定上下文和响应类型。
     *
     * @param <C> 上下文的类型
     * @param <R> 响应的类型
     * @return 一个新的 Pipeline 实例
     */
    <C, R> Pipeline<C, R> create();
}
