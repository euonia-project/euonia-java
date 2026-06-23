package com.euonia.http;

import com.euonia.core.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Configuration for request context related beans.
 */
@Configuration
public class HttpContextAccessorConfiguration {

    @Bean
    @RequestScope
    public RequestContextAccessor requestContextAccessor(RequestContext context) {
        if (context.getTraceIdentifier() == null || context.getTraceIdentifier().isBlank()) {
            context.setTraceIdentifier(ObjectId.newGuid().toString());
        }
        var accessor = new DefaultRequestContextAccessor();
        accessor.setRequestContext(context);
        return accessor;
    }
}
