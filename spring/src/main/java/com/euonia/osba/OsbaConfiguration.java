package com.euonia.osba;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.euonia.factory.BusinessObjectFactory;
import com.euonia.factory.ObjectFactory;
import com.euonia.reflection.ServiceProvider;

/**
 * OSBA（对象服务业务抽象）的 Spring 配置类。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class OsbaConfiguration {
    /**
     * 创建 {@link ObjectFactory} Bean，使用基于 {@link ServiceProvider} 的业务对象工厂实现。
     *
     * @param resolver 服务提供者
     * @return 业务对象工厂实例
     */
    @Bean
    public ObjectFactory objectFactory(ServiceProvider resolver) {
        return new BusinessObjectFactory(resolver);
    }
}
