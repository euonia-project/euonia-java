package com.euonia.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 请求上下文相关 Bean 的配置类。
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class HttpContextAccessorConfiguration {

    /**
     * 创建 {@link RequestContextAccessor} Bean，使用默认的单例实现。
     *
     * @return 默认请求上下文访问器实例
     */
    @Bean
    public RequestContextAccessor requestContextAccessor() {
        return DefaultRequestContextAccessor.getInstance();
    }
}
