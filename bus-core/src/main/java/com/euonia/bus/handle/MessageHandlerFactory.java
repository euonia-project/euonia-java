package com.euonia.bus.handle;

import com.euonia.reflection.ServiceProvider;

/**
 * 消息处理器工厂的函数式接口，用于通过 {@link ServiceProvider} 创建 {@link MessageHandler} 实例。
 *
 * @author damon(zhaorong@outlook.com)
 */
@FunctionalInterface
public interface MessageHandlerFactory {
    /**
     * 创建一个新的 {@link MessageHandler} 实例。
     *
     * @param provider 用于解析依赖的服务提供者
     * @return 一个新的 {@link MessageHandler} 实例
     */
    MessageHandler createHandler(ServiceProvider provider);
}
