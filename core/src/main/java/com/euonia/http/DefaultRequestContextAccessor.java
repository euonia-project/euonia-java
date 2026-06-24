package com.euonia.http;

public class DefaultRequestContextAccessor implements RequestContextAccessor {
    private final ThreadLocal<RequestContext> requestContext = new ThreadLocal<>();

    @Override
    public void setContext(RequestContext context) {
        requestContext.set(context);
    }

    @Override
    public void removeContext() {
        requestContext.remove();
    }

    @Override
    public RequestContext getContext() {
        return requestContext.get();
    }
}
