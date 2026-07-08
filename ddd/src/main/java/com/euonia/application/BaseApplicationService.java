package com.euonia.application;

import java.util.Optional;

import com.euonia.reflection.ServiceProvider;
import com.euonia.security.UserPrincipal;

/**
 * {@link BaseApplicationService} 是应用服务的抽象基类，提供通用功能。
 * 它实现了 {@link ApplicationService} 接口，并使用 {@link ServiceProvider} 来访问其他服务，例如代表当前已认证用户的 {@link UserPrincipal}。
 * <p>
 * 具体的应用服务实现可以继承此类，从而获得解析服务和访问用户信息的能力，无需在每个服务中实现这些功能。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class BaseApplicationService implements ApplicationService {

    /**
     * 服务提供者
     */
    protected final ServiceProvider provider;

    /**
     * 使用指定的服务提供者构造基类实例。
     *
     * @param provider 服务提供者
     */
    protected BaseApplicationService(ServiceProvider provider) {
        this.provider = provider;
    }

    /**
     * 从 {@link ServiceProvider} 中解析指定类型的服务。
     *
     * @param <T>  要解析的服务类型
     * @param type 要解析的服务类类型
     * @return 包含已解析服务的 {@link Optional}，如果服务不可用则为空
     */
    protected <T> Optional<T> getService(Class<T> type) {
        return provider.getService(type);
    }

    /**
     * 从 {@link ServiceProvider} 中获取当前已认证的用户。
     *
     * @return 当前已认证的用户，如果不可用则为 null
     */
    protected UserPrincipal getUser() {
        return getService(UserPrincipal.class).orElse(null);
    }
}
