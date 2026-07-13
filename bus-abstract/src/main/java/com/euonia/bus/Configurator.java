package com.euonia.bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.convention.MessageConventionBuilder;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.bus.strategy.TransportStrategyBuilder;

/**
 * {@link Configurator} 是配置消息总线的主接口。
 * 提供了配置消息约定、传输策略和处理器注册的方法。
 * 此接口的实现将在总线启动之前用于设置总线。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Configurator {
    /**
     * 获取默认传输策略名称，可用于消息路由和分类。
     *
     * @return 默认传输策略名称
     */
    String getDefaultTransport();

    /**
     * 获取已配置的 {@link MessageConventionBuilder}。
     *
     * @return MessageConventionBuilder 实例
     */
    MessageConventionBuilder getConventionBuilder();

    /**
     * 获取已配置的传输策略构建器映射。
     *
     * @return 传输策略构建器映射
     */
    ConcurrentMap<String, TransportStrategyBuilder> getStrategyBuilders();

    /**
     * 获取已配置的处理器注册信息映射。
     *
     * @return 处理器注册信息映射
     */
    Map<String, ChannelRegistration> getRegistrations();

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
