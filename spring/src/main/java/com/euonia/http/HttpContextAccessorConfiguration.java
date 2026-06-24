package com.euonia.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for request context related beans.
 */
@Configuration
public class HttpContextAccessorConfiguration {

    @Bean
    public RequestContextAccessor requestContextAccessor() {
        return new DefaultRequestContextAccessor();
    }
}
