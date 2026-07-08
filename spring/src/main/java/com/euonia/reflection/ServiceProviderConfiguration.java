package com.euonia.reflection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务提供者的 Spring 配置类，将 {@link ServiceProvider} 注册为 Spring Bean。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class ServiceProviderConfiguration {
    /**
     * 创建 {@link ServiceProvider} Bean，基于 Spring {@link ApplicationContext}。
     *
     * @param applicationContext Spring 应用上下文
     * @return 基于 ApplicationContext 的服务提供者实例
     */
    @Bean
    public ServiceProvider serviceProvider(ApplicationContext applicationContext) {
        return new ApplicationContextServiceProvider(applicationContext);
    }
}
