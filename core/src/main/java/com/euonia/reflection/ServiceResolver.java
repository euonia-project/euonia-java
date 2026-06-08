package com.euonia.reflection;

public interface ServiceResolver {
    <T> T getService(Class<T> type);

    <T> T getRequiredService(Class<T> type);

    <T> T createInstance(Class<T> type, Object... constructorArguments);

    default <T> T getServiceOrCreate(Class<T> type, Object... constructorArguments) {
        T service = getService(type);
        return service != null ? service : createInstance(type, constructorArguments);
    }
}
