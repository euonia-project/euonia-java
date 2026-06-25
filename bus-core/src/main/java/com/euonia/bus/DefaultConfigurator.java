package com.euonia.bus;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import com.euonia.bus.convention.DefaultMessageConventionBuilder;
import com.euonia.bus.convention.MessageConventionBuilder;
import com.euonia.bus.strategy.DefaultTransportStrategyBuilder;
import com.euonia.bus.strategy.TransportStrategyBuilder;
import com.euonia.utility.Assert;

/**
 * DefaultConfigurator 是 {@link Configurator} 接口的默认实现。
 * 它提供了用于配置消息约定、传输策略和处理器注册的流式 API。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultConfigurator implements Configurator {
    private final MessageConventionBuilder conventionBuilder = new DefaultMessageConventionBuilder();
    private final ConcurrentMap<String, TransportStrategyBuilder> strategyBuilders = new ConcurrentHashMap<>();
    private final List<HandlerRegistration> registrations = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * 获取消息约定构建器。
     *
     * @return 消息约定构建器
     */
    @Override
    public MessageConventionBuilder getConventionBuilder() {
        return conventionBuilder;
    }

    /**
     * 获取传输策略构建器映射。
     *
     * @return 传输策略构建器映射
     */
    @Override
    public ConcurrentMap<String, TransportStrategyBuilder> getStrategyBuilders() {
        return strategyBuilders;
    }

    /**
     * 获取已注册的处理器列表。
     *
     * @return 已注册的处理器列表
     */
    @Override
    public List<HandlerRegistration> getRegistrations() {
        return registrations;
    }

    /**
     * 配置消息约定。
     *
     * @param conventionConfig 消息约定配置
     * @return 当前配置器实例
     */
    public DefaultConfigurator setConvention(Consumer<MessageConventionBuilder> conventionConfig) {
        Assert.notNull(conventionConfig, "Convention configuration cannot be null");
        conventionConfig.accept(conventionBuilder);
        return this;
    }

    /**
     * 配置传输策略。
     *
     * @param name           策略名称
     * @param strategyConfig 策略配置
     * @return 当前配置器实例
     */
    public DefaultConfigurator setStrategy(String name, Consumer<TransportStrategyBuilder> strategyConfig) {
        Assert.notNull(strategyConfig, "Strategy configuration cannot be null");
        Assert.notEmpty(name, "Strategy name cannot be null or empty");
        var builder = strategyBuilders.computeIfAbsent(name, k -> new DefaultTransportStrategyBuilder());
        strategyConfig.accept(builder);
        return this;
    }

    /**
     * 注册处理器。
     *
     * @param registration 处理器注册信息
     * @return 当前配置器实例
     */
    public DefaultConfigurator registerHandlers(HandlerRegistration registration) {
        Assert.notNull(registration, "Message registration cannot be null");
        registrations.add(registration);
        return this;
    }

    /**
     * 注册处理器。
     *
     * @param types 处理器类型列表
     * @return 当前配置器实例
     */
    public DefaultConfigurator registerHandlers(List<Class<?>> types) {
        Assert.notEmpty(types, "Types cannot be null or empty");
        if (!types.isEmpty()) {
            var handlerTypes = MessageHandlerFinder.find(types.toArray(Class<?>[]::new));
            this.registrations.addAll(handlerTypes);
        }
        return this;
    }

    /**
     * 注册处理器。
     *
     * @param types 处理器类型数组
     * @return 当前配置器实例
     */
    public DefaultConfigurator registerHandlers(Class<?>... types) {
        Assert.notEmpty(types, "Types cannot be null or empty");
        if (types.length > 0) {
            var handlerTypes = MessageHandlerFinder.find(types);
            this.registrations.addAll(handlerTypes);
        }

        return this;
    }

    /**
     * 注册处理器。
     *
     * @param packageNames 包名数组
     * @return 当前配置器实例
     */
    public DefaultConfigurator registerHandlers(String... packageNames) {
        Assert.notContains(packageNames, String::isBlank, "Package names cannot contain null or empty values");

        for (String packageName : packageNames) {
            var handlerTypes = MessageHandlerFinder.find(packageName);
            this.registrations.addAll(handlerTypes);
        }
        return this;
    }
}
