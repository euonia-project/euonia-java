package com.euonia.bus.handle;

import com.euonia.reflection.ServiceProvider;

/**
 * 处理器委托工厂的函数式接口，用于通过 {@link ServiceProvider} 创建 {@link HandlerDelegate} 实例。
 *
 * @author damon(zhaorong@outlook.com)
 */
@FunctionalInterface
public interface HandlerFactory {
    /**
     * 创建一个新的 {@link HandlerDelegate} 实例。
     *
     * @param provider 用于解析依赖的服务提供者
     * @return 一个新的 {@link HandlerDelegate} 实例
     */
    HandlerDelegate createDelegate(ServiceProvider provider);
}
