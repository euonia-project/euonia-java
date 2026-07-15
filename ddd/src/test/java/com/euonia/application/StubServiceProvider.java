package com.euonia.application;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.euonia.reflection.ServiceProvider;

/**
 * {@link ServiceProvider} 的轻量测试桩，用于 {@link BaseApplicationServiceTest}。
 */
class StubServiceProvider implements ServiceProvider {

    private final Map<Class<?>, Object> services = new HashMap<>();

    /**
     * 创建一个预注册了指定服务的桩实例。
     */
    static <T> StubServiceProvider with(Class<T> type, T instance) {
        var stub = new StubServiceProvider();
        stub.services.put(type, instance);
        return stub;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getService(Class<T> type) {
        return Optional.ofNullable((T) services.get(type));
    }

    @Override
    public <T> Optional<T> getService(Class<T> type, Class<?>... genericTypeArguments) {
        return getService(type);
    }

    @Override
    public <T> Optional<T> getService(Class<T> type, String serviceName) {
        return getService(type);
    }

    @Override
    public <T> T getRequiredService(Class<T> type) {
        return getService(type).orElseThrow(() ->
            new IllegalStateException("Service not found: " + type.getName()));
    }

    @Override
    public <T> List<T> getServices(Class<T> type) {
        return getService(type).map(List::of).orElseGet(List::of);
    }

    @Override
    public <T> List<T> getServices(Class<T> type, Class<?>... genericTypeArguments) {
        return getServices(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> type, Object... constructorArguments) {
        try {
            Constructor<?>[] constructors = type.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == constructorArguments.length) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    boolean match = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (constructorArguments[i] != null
                            && !paramTypes[i].isAssignableFrom(constructorArguments[i].getClass())) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        constructor.setAccessible(true);
                        return (T) constructor.newInstance(constructorArguments);
                    }
                }
            }
            throw new IllegalStateException(
                "No matching constructor found for " + type.getName()
                    + " with " + Arrays.toString(constructorArguments));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Failed to create instance of " + type.getName(), e);
        }
    }
}
