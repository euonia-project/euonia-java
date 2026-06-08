package com.euonia.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleServiceResolver implements ServiceResolver {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T instance) {
        services.put(type, instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            return null;
        }
        return (T) service;
    }

    @Override
    public <T> T getRequiredService(Class<T> type) {
        T service = getService(type);
        if (service == null) {
            throw new IllegalStateException("Can not resolve service: " + type.getName());
        }
        return service;
    }

    @Override
    public <T> T createInstance(Class<T> type, Object... constructorArguments) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != constructorArguments.length) {
                continue;
            }

            if (!isAssignable(parameterTypes, constructorArguments)) {
                continue;
            }

            try {
                constructor.setAccessible(true);
                @SuppressWarnings("unchecked")
                T created = (T) constructor.newInstance(constructorArguments);
                return created;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not construct type: " + type.getName(), e);
            }
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Could not construct type: " + type.getName(), e);
        }
    }

    private static boolean isAssignable(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            if (!parameterTypes[i].isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }
}
