package com.euonia.osba;

import java.util.Objects;
import java.util.function.Function;

/**
 * BusinessContext provides a small service-resolution and instance-creation
 * gateway for business objects created by OSBA factories.
 */
public class BusinessContext {
    private final Function<Class<?>, ?> serviceResolver;
    private final Function<Class<?>, ?> instanceCreator;

    public BusinessContext(Function<Class<?>, ?> serviceResolver, Function<Class<?>, ?> instanceCreator) {
        this.serviceResolver = serviceResolver;
        this.instanceCreator = instanceCreator;
    }

    public <T> T getRequiredService(Class<T> serviceType) {
        T service = getService(serviceType);
        if (service == null) {
            throw new IllegalStateException("Missing required service: " + serviceType.getName());
        }
        return service;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceType) {
        if (serviceResolver == null) {
            return null;
        }
        Object service = serviceResolver.apply(serviceType);
        if (service == null) {
            return null;
        }
        return (T) service;
    }

    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> objectType) {
        Objects.requireNonNull(objectType, "objectType");
        if (instanceCreator == null) {
            throw new IllegalStateException("BusinessContext does not support instance creation.");
        }
        return (T) instanceCreator.apply(objectType);
    }
}
