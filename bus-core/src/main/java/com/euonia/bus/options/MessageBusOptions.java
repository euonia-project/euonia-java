package com.euonia.bus.options;

import java.util.List;

import com.euonia.bus.Configurator;
import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.strategy.TransportStrategy;

/**
 * 定义配置消息总线的选项，包括传输策略、消息约定和管道行为。
 * 该类是不可变的，可用于使用指定的选项创建消息总线实例。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class MessageBusOptions {
    private final Configurator configurator;
    private String defaultTransport;

    private boolean isEnablePipelineBehaviors = true;

    /**
     * 使用指定的配置器创建 {@link MessageBusOptions} 的新实例。
     *
     * @param configurator 用于配置消息总线的配置器
     */
    public MessageBusOptions(Configurator configurator) {
        this.configurator = configurator;
    }

    /**
     * 获取默认传输策略名称，可用于消息路由和分类。
     *
     * @return 默认传输策略名称
     */
    public String getDefaultTransport() {
        return defaultTransport;
    }

    /**
     * 设置默认传输策略名称，可用于消息路由和分类。
     *
     * @param defaultTransport 默认传输策略名称
     */
    public void setDefaultTransport(String defaultTransport) {
        this.defaultTransport = defaultTransport;
    }

    /**
     * 获取是否启用管道行为，可用于消息处理和操作。
     *
     * @return 如果管道行为已启用则返回 true，否则返回 false
     */
    public boolean isEnablePipelineBehaviors() {
        return isEnablePipelineBehaviors;
    }

    /**
     * 设置是否启用管道行为，可用于消息处理和操作。
     *
     * @param enablePipelineBehaviors true 表示启用管道行为，false 表示禁用
     */
    public void setEnablePipelineBehaviors(boolean enablePipelineBehaviors) {
        isEnablePipelineBehaviors = enablePipelineBehaviors;
    }

    /**
     * 获取消息约定，可用于消息格式化和验证。
     *
     * @return 消息约定
     */
    public MessageConvention getConvention() {
        return configurator.getConventionBuilder().getConvention();
    }

    /**
     * 获取传输策略名称列表，可用于消息路由和分类。
     *
     * @return 传输策略名称列表
     */
    public List<String> getStrategyAssignedTypes() {
        return configurator.getStrategyBuilders().keySet().stream().toList();
    }

    /**
     * 获取指定传输名称对应的传输策略，可用于消息路由和分类。
     *
     * @param transport 传输名称
     * @return 指定传输名称对应的传输策略，如果未找到则返回 null
     */
    public TransportStrategy getStrategy(String transport) {
        var builder = configurator.getStrategyBuilders().get(transport);
        return builder == null ? null : builder.getStrategy();
    }
}
