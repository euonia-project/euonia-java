package com.euonia.sample;

import com.euonia.core.ObjectId;
import com.euonia.http.DefaultRequestContextAccessor;
import com.euonia.http.RequestContext;
import jakarta.servlet.*;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class HttpContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            var context = getRequestContext((RequestFacade) request);
            DefaultRequestContextAccessor.set(context);
            chain.doFilter(request, response);
        } finally {
            DefaultRequestContextAccessor.remove();
        }
    }

    private RequestContext getRequestContext(RequestFacade request) {
        var requestContext = new RequestContext();
        requestContext.setRequestUri(request.getRequestURI());
        requestContext.setRemotePort(request.getRemotePort());
        requestContext.setRequestMethod(request.getMethod());
        requestContext.setRemoteIpAddress(request.getRemoteAddr());
        requestContext.setTraceIdentifier(ObjectId.newGuid().toString());

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
