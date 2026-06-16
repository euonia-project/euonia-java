package com.euonia.pipeline;

import com.euonia.reflection.ServiceProvider;

/**
 * DefaultPipelineFactory 是 PipelineFactory 的默认实现，负责创建 Pipeline 的实例。
 * 它使用 ServiceProvider 来解析和实例化管道组件。
 *
 * @author damon(zhaorong@outlook)
 */
public class DefaultPipelineFactory implements PipelineFactory {
    private final ServiceProvider provider;

    /**
     * 构造函数，接受一个 ServiceProvider 实例。
     *
     * @param provider 用于解析和实例化管道组件的 ServiceProvider
     */
    public DefaultPipelineFactory(ServiceProvider provider) {
        this.provider = provider;
    }

    /**
     * 创建一个新的 Pipeline 实例，并指定上下文和响应类型。
     *
     * @param <C> 上下文的类型
     * @param <R> 响应的类型
     * @return 一个新的 Pipeline 实例
     */
    @Override
    public <C, R> Pipeline<C, R> create() {
        return new DefaultPipelineProvider<>(provider);
    }
}
