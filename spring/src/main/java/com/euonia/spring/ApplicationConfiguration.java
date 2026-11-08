package com.euonia.spring;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.euonia.reflection.ServiceProvider;
import com.euonia.usecase.DefaultUseCaseExecutor;
import com.euonia.usecase.UseCaseExecutor;

/**
 * 应用层 Spring 配置。
 * <p>
 * 与 .NET 版 {@code ApplicationModule} 对齐，注册用例执行器，并为应用服务
 * 自动注入服务提供者。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * 注册默认用例执行器。
     *
     * @param serviceProvider 服务提供者，用于解析用例实例
     * @return 默认用例执行器
     */
    @Bean
    public UseCaseExecutor useCaseExecutor(ServiceProvider serviceProvider) {
        return new DefaultUseCaseExecutor(serviceProvider);
    }

    /**
     * 注册应用服务 Bean 后处理器。
     * <p>
     * 使用静态工厂方法以便后处理器在其它 Bean 创建之前完成注册。
     *
     * @return 应用服务 Bean 后处理器
     */
    @Bean
    public static BeanPostProcessor applicationServiceBeanPostProcessor() {
        return new ApplicationServiceBeanPostProcessor();
    }
}
