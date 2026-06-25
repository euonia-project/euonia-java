package com.euonia.http;

import java.util.logging.Logger;

public final class DefaultRequestContextAccessor implements RequestContextAccessor {
    private static final Logger LOGGER = Logger.getLogger(DefaultRequestContextAccessor.class.getName());

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    private static final DefaultRequestContextAccessor INSTANCE = new DefaultRequestContextAccessor();

    private DefaultRequestContextAccessor() {
    }

    public static DefaultRequestContextAccessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void setContext(RequestContext context) {
        HOLDER.set(context);
    }

    @Override
    public void removeContext() {
        HOLDER.remove();
    }

    @Override
    public RequestContext getContext() {
        return HOLDER.get();
    }

    public static RequestContext get() {
        return getInstance().getContext();
    }

    public static void set(RequestContext context) {
        getInstance().setContext(context);
    }

    public static void remove() {
        getInstance().removeContext();
    }
}
