package com.euonia.sample;

import com.euonia.http.RequestContext;
import com.euonia.spring.BeanScope;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class RequestContextConfiguration {
    @Bean
    @Scope(BeanScope.REQUEST)
    public RequestContext requestContext() {
        var requestContext = new RequestContext();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        requestContext.setRequestUri(request.getRequestURL().toString());
        requestContext.setRemotePort(request.getRemotePort());
        requestContext.setRequestMethod(request.getMethod());
        requestContext.setRemoteIpAddress(request.getRemoteAddr());

        Map<String, String> headers = Collections.list(request.getHeaderNames())
                                                 .stream()
                                                 .collect(Collectors.toMap(
                                                     headerName -> headerName,
                                                     headerName -> String.join(",", Collections.list(request.getHeaders(headerName)))
                                                 ));

        requestContext.setRequestHeaders(headers);
        return requestContext;
    }
}
