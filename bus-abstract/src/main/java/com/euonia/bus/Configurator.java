package com.euonia.bus;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.convention.MessageConventionBuilder;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.bus.strategy.TransportStrategyBuilder;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * BusConfigurator is the main interface for configuring the message bus.
 * It provides methods to configure message conventions, transport strategies, and handler registrations.
 * Implementations of this interface will be used to set up the bus before it is started.
 */
public interface Configurator {
    /**
     * 获取默认传输策略名称，可用于消息路由和分类。
     *
     * @return 默认传输策略名称
     */
    String getDefaultTransport();

    /**
     * 获取是否启用管道行为，可用于消息处理和操作。
     *
     * @return 如果管道行为已启用则返回 true，否则返回 false
     */
    boolean isEnablePipelineBehaviors();

    /**
     * Gets the MessageConventionBuilder that has been configured.
     *
     * @return the MessageConventionBuilder
     */
    MessageConventionBuilder getConventionBuilder();

    /**
     * Gets the map of transport strategy builders that have been configured.
     *
     * @return the map of transport strategy builders
     */
    ConcurrentMap<String, TransportStrategyBuilder> getStrategyBuilders();

    /**
     * Gets the list of handler registrations that have been configured.
     *
     * @return the list of handler registrations
     */
    List<HandlerRegistration> getRegistrations();

    /**
     * 获取消息约定，可用于消息格式化和验证。
     *
     * @return 消息约定
     */
    default MessageConvention getConvention() {
        return getConventionBuilder().getConvention();
    }

    /**
     * 获取传输策略名称列表，可用于消息路由和分类。
     *
     * @return 传输策略名称列表
     */
    default List<String> getStrategyAssignedTypes() {
        return getStrategyBuilders().keySet().stream().toList();
    }

    /**
     * 获取指定传输名称对应的传输策略，可用于消息路由和分类。
     *
     * @param transport 传输名称
     * @return 指定传输名称对应的传输策略，如果未找到则返回 null
     */
    default TransportStrategy getStrategy(String transport) {
        var builder = getStrategyBuilders().get(transport);
        return builder == null ? null : builder.getStrategy();
    }
}
