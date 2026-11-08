package com.euonia.reflection;

/**
 * 由需要延迟解析服务的组件实现，用于暴露 {@link ServiceProvider}。
 * <p>
 * 对应 .NET 版的 {@code IHasLazyServiceProvider}。实现类型通常由依赖注入容器
 * （如 Spring 的 BeanPostProcessor）在初始化阶段注入服务提供者。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface HasLazyServiceProvider {

    /**
     * 获取用于按需解析服务的提供者。
     *
     * @return 延迟服务提供者；尚未注入时为 {@code null}
     */
    ServiceProvider getLazyServiceProvider();

    /**
     * 设置用于按需解析服务的提供者。
     *
     * @param lazyServiceProvider 延迟服务提供者
     */
    void setLazyServiceProvider(ServiceProvider lazyServiceProvider);
}
