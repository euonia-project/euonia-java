package com.euonia.reflection;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * 延迟解析服务的 {@link ServiceProvider} 实现。
 * <p>
 * 服务实例在首次访问时解析，并按服务标识（服务类型、泛型参数或服务名称）缓存，
 * 后续访问返回同一实例。与 .NET 版 {@code LazyServiceProvider} 的行为保持一致。
 * <p>
 * 缓存的生命周期与当前实例一致，因此应将 {@code LazyServiceProvider} 与作用域
 * （如 Spring 的 request/session 作用域）绑定使用；若绑定到单例，则单例作用域内的
 * 服务会被长期缓存。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class LazyServiceProvider implements ServiceProvider {

    /**
     * 实际的服务提供者。
     */
    private final ServiceProvider provider;

    /**
     * 已解析服务的缓存。
     */
    private final ConcurrentMap<ServiceKey, Optional<?>> cachedServices = new ConcurrentHashMap<>();

    /**
     * 使用实际的服务提供者构造延迟服务提供者。
     *
     * @param provider 实际的服务提供者
     * @throws NullPointerException 当 {@code provider} 为 {@code null} 时抛出
     */
    public LazyServiceProvider(ServiceProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider cannot be null.");
    }

    /**
     * 创建延迟服务提供者。
     *
     * @param provider 实际的服务提供者
     * @return 延迟服务提供者
     */
    public static LazyServiceProvider of(ServiceProvider provider) {
        return provider instanceof LazyServiceProvider lazy ? lazy : new LazyServiceProvider(provider);
    }

    /**
     * 获取实际的服务提供者。
     *
     * @return 实际的服务提供者
     */
    public ServiceProvider getProvider() {
        return provider;
    }

    @Override
    public <T> Optional<T> getService(Class<T> type) {
        return resolve(new ServiceKey(type, null, null, LookupKind.SERVICE),
                       () -> provider.getService(type));
    }

    @Override
    public <T> Optional<T> getService(Class<T> type, Class<?>... genericTypeArguments) {
        var key = new ServiceKey(type, listOf(genericTypeArguments), null, LookupKind.GENERIC_SERVICE);
        return resolve(key, () -> provider.getService(type, genericTypeArguments));
    }

    @Override
    public <T> Optional<T> getService(Class<T> type, String serviceName) {
        var key = new ServiceKey(type, null, serviceName, LookupKind.NAMED_SERVICE);
        return resolve(key, () -> provider.getService(type, serviceName));
    }

    @Override
    public <T> T getRequiredService(Class<T> type) {
        return getService(type).orElseThrow(() ->
            new IllegalStateException("Service not found: " + type.getName()));
    }

    @Override
    public <T> List<T> getServices(Class<T> type) {
        return resolve(new ServiceKey(type, null, null, LookupKind.SERVICES),
                       () -> Optional.of(List.copyOf(provider.getServices(type))))
            .orElseGet(List::of);
    }

    @Override
    public <T> List<T> getServices(Class<T> type, Class<?>... genericTypeArguments) {
        var key = new ServiceKey(type, listOf(genericTypeArguments), null, LookupKind.GENERIC_SERVICES);
        return resolve(key, () -> Optional.of(List.copyOf(provider.getServices(type, genericTypeArguments))))
            .orElseGet(List::of);
    }

    @Override
    public <T> T createInstance(Class<T> type, Object... constructorArguments) {
        return provider.createInstance(type, constructorArguments);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> resolve(ServiceKey key, Supplier<Optional<T>> factory) {
        return (Optional<T>) cachedServices.computeIfAbsent(key, ignored -> factory.get());
    }

    private static List<Class<?>> listOf(Class<?>[] types) {
        return types == null || types.length == 0 ? null : List.of(types);
    }

    /**
     * 服务查找方式。
     */
    private enum LookupKind {
        SERVICE,
        GENERIC_SERVICE,
        NAMED_SERVICE,
        SERVICES,
        GENERIC_SERVICES
    }

    /**
     * 服务缓存的键。
     *
     * @param type          服务类型
     * @param genericTypes  泛型参数；没有时为 {@code null}
     * @param serviceName   服务名称；没有时为 {@code null}
     * @param kind          查找方式
     */
    private record ServiceKey(Class<?> type, List<Class<?>> genericTypes, String serviceName, LookupKind kind) {
    }
}
