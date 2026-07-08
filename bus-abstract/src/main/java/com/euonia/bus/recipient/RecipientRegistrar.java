package com.euonia.bus.recipient;

import java.util.Map;

import com.euonia.bus.ChannelRegistration;

/**
 * {@link RecipientRegistrar} 负责将处理器注册信息注册到接收者系统中。
 * 提供了将处理器注册列表与默认传输一起注册的方法。
 * <p>
 * 此接口抽象了注册过程，允许不同的实现处理处理器注册的具体方式以及如何使用默认传输。
 * <p>
 * 此接口的实现可能会与各种接收者类型（如订阅者、执行器）进行交互，
 * 并管理处理器注册的生命周期，确保它们被正确注册并可用于消息分发。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface RecipientRegistrar {
    /**
     * 使用指定的默认传输将处理器注册信息注册到接收者系统中。
     *
     * @param registrations    要注册的处理器注册信息
     * @param defaultTransport 要使用的默认传输
     */
    void register(Map<String, ChannelRegistration> registrations, String defaultTransport);
}
