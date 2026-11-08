package com.euonia.application;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euonia.bus.Bus;
import com.euonia.http.RequestContext;
import com.euonia.http.RequestContextAccessor;
import com.euonia.reflection.LazyServiceProvider;
import com.euonia.reflection.ServiceProvider;
import com.euonia.security.UserPrincipal;
import com.euonia.usecase.UseCaseExecutor;

/**
 * {@link BaseApplicationService} 是应用服务的抽象基类，提供通用功能。
 * <p>
 * 与 .NET 版 {@code BaseApplicationService} 保持一致，通过 {@link #getLazyServiceProvider()
 * 延迟服务提供者} 按需解析常用服务（消息总线、当前用户、日志、用例执行器），
 * 并通过 {@link RequestContextAccessor} 访问当前请求上下文。
 * 派生应用服务因此无需重复编写依赖解析代码。
 * <p>
 * 依赖注入容器（如 Spring 的 {@code ApplicationServiceBeanPostProcessor}）会在 Bean
 * 初始化阶段自动注入服务提供者；也可以直接通过构造函数传入。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class BaseApplicationService implements ApplicationService {

    /**
     * 延迟服务提供者。
     */
    private volatile ServiceProvider lazyServiceProvider;

    /**
     * 创建一个尚未注入服务提供者的基类实例。
     * <p>
     * 该构造函数供依赖注入容器使用，服务提供者会在初始化阶段通过
     * {@link #setLazyServiceProvider(ServiceProvider)} 注入。
     */
    protected BaseApplicationService() {
    }

    /**
     * 使用指定的服务提供者构造基类实例。
     *
     * @param provider 服务提供者
     */
    protected BaseApplicationService(ServiceProvider provider) {
        setLazyServiceProvider(provider);
    }

    /**
     * 获取延迟服务提供者。
     *
     * @return 延迟服务提供者；尚未注入时为 {@code null}
     */
    @Override
    public ServiceProvider getLazyServiceProvider() {
        return lazyServiceProvider;
    }

    /**
     * 设置延迟服务提供者。
     * <p>
     * 传入的服务提供者会被包装为 {@link LazyServiceProvider}，从而按服务标识缓存解析结果。
     * 若传入的已经是 {@link LazyServiceProvider}，则直接使用。
     *
     * @param lazyServiceProvider 服务提供者
     */
    @Override
    public void setLazyServiceProvider(ServiceProvider lazyServiceProvider) {
        this.lazyServiceProvider = lazyServiceProvider == null
            ? null
            : LazyServiceProvider.of(lazyServiceProvider);
    }

    /**
     * 从服务提供者中解析指定类型的服务。
     *
     * @param <T>  要解析的服务类型
     * @param type 要解析的服务类类型
     * @return 包含已解析服务的 {@link Optional}，如果服务不可用则为空
     * @throws IllegalStateException 当服务提供者尚未注入时抛出
     */
    protected <T> Optional<T> getService(Class<T> type) {
        return requireProvider().getService(type);
    }

    /**
     * 从服务提供者中解析指定类型的必需服务。
     *
     * @param <T>  要解析的服务类型
     * @param type 要解析的服务类类型
     * @return 已解析的服务
     * @throws IllegalStateException 当服务提供者尚未注入或服务不可用时抛出
     */
    protected <T> T getRequiredService(Class<T> type) {
        return requireProvider().getRequiredService(type);
    }

    /**
     * 获取消息总线。
     *
     * @return {@link Bus} 实例；未注册时为 {@code null}
     */
    protected Bus getBus() {
        return getServiceOrNull(Bus.class);
    }

    /**
     * 获取当前请求的用户主体。
     * <p>
     * 优先从服务提供者解析 {@link UserPrincipal}（与 .NET 版一致）；
     * 若容器中未注册该服务，则回退到当前请求上下文。
     *
     * @return 当前已认证的用户，如果不可用则为 {@code null}
     */
    protected UserPrincipal getUser() {
        var user = getServiceOrNull(UserPrincipal.class);
        if (user != null) {
            return user;
        }

        var context = RequestContextAccessor.get();
        return context == null ? null : context.getUser();
    }

    /**
     * 获取当前请求上下文。
     *
     * @return 当前请求上下文；不在请求流内时为 {@code null}
     */
    protected RequestContext getRequestContext() {
        return RequestContextAccessor.get();
    }

    /**
     * 获取以当前服务类型为类别的日志记录器。
     *
     * @return 日志记录器
     */
    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    /**
     * 获取用例执行器，用于执行用例并把结果分发到 presenter。
     *
     * @return {@link UseCaseExecutor} 实例；未注册时为 {@code null}
     */
    protected UseCaseExecutor getExecutor() {
        return getServiceOrNull(UseCaseExecutor.class);
    }

    private ServiceProvider requireProvider() {
        var provider = lazyServiceProvider;
        if (provider == null) {
            throw new IllegalStateException(
                "LazyServiceProvider has not been initialized for " + getClass().getName()
                    + ". Inject a ServiceProvider or pass it to the constructor.");
        }
        return provider;
    }

    private <T> T getServiceOrNull(Class<T> type) {
        var provider = lazyServiceProvider;
        return provider == null ? null : provider.getService(type).orElse(null);
    }
}
