package com.euonia.pipeline;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.euonia.reflection.ServiceProvider;
import com.euonia.spring.BeanScope;

/**
 * 管道 Spring 配置类，负责注册管道相关的 Bean。
 * <p>
 * 配置以下 Bean：
 * <ul>
 *   <li>{@link PipelineFactory} —— 管道工厂，用于创建管道实例</li>
 *   <li>{@link Pipeline} —— 管道实例，用于编排消息处理步骤</li>
 *   <li>{@link PipelineDelegate} —— 管道委托，封装已构建的管道执行入口</li>
 * </ul>
 * 所有 Bean 均以原型作用域注册，确保每次注入都获得新实例。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class PipelineConfiguration {
    /**
     * 注册 {@link PipelineFactory} Bean，使用默认的管道工厂实现。
     *
     * @param resolver 服务提供者，用于依赖解析
     * @return 默认管道工厂实例
     */
    @Bean
    @Scope(BeanScope.PROTOTYPE)
    public PipelineFactory pipelineFactory(ServiceProvider resolver) {
        return new DefaultPipelineFactory(resolver);
    }

    /**
     * 注册 {@link Pipeline} Bean，使用默认的管道提供者实现。
     *
     * @param resolver 服务提供者，用于依赖解析
     * @return 默认管道实例
     */
    @Bean
    @Scope(BeanScope.PROTOTYPE)
    @SuppressWarnings("rawtypes")
    public Pipeline pipeline(ServiceProvider resolver) {
        return new DefaultPipelineProvider<>(resolver);
    }

    /**
     * 注册 {@link PipelineDelegate} Bean，从已构建的管道生成委托。
     *
     * @param pipeline 用于构建委托的管道实例
     * @return 封装已构建管道的管道委托
     */
    @Bean
    @Scope(BeanScope.PROTOTYPE)
    @SuppressWarnings({"rawtypes"})
    public PipelineDelegate pipelineDelegate(Pipeline pipeline) {
        return pipeline.build();
    }
}
