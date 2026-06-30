package com.euonia.bus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class DefaultConfigurator implements Configurator {
    private final MessageConventionBuilder conventionBuilder = new DefaultMessageConventionBuilder();
    private final ConcurrentMap<String, TransportStrategyBuilder> strategyBuilders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ChannelRegistration> registrations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Class<?>> channelMessageTypeCache = new ConcurrentHashMap<>();

    private Supplier<String> defaultTransportSupplier = () -> "";
    private Supplier<Boolean> enablePipelineBehaviorsSupplier = () -> true;

    @Override
    public String getDefaultTransport() {
        return defaultTransportSupplier.get();
    }

    @Override
    public boolean isEnablePipelineBehaviors() {
        return enablePipelineBehaviorsSupplier.get();
    }

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
    public Map<String, ChannelRegistration> getRegistrations() {
        return Collections.unmodifiableMap(registrations);
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
     * @param channel     通道名称
     * @param messageType 消息类型
     * @param handler     处理器函数
     * @param <T>         消息类型
     * @param <R>         响应类型
     * @return 当前配置器实例
     */
    public <T, R> DefaultConfigurator registerHandler(String channel, Class<T> messageType, BiFunction<T, MessageContext, R> handler) {
        try {
            var method = LambdaHandler.class.getDeclaredMethod("handle", Object.class, MessageContext.class);
            return registerHandlers(channel, messageType, new ChannelHandler(LambdaHandler.class, method, new LambdaHandler<>(handler)));
        } catch (NoSuchMethodException e) {
            return this;
        }
    }

    public DefaultConfigurator registerHandlers(String channel, Class<?> messageType, ChannelHandler handler) {
        Assert.notNull(messageType, "Message type cannot be null");
        Assert.notNull(handler, "Handling cannot be null");

        var registration = registrations.putIfAbsent(channel, new ChannelRegistration(messageType));
        if (registration == null) {
            throw new IllegalStateException("Duplicate handler for channel: " + channel);
        }
        if (messageType != registration.getMessageType()) {
            throw new IllegalStateException("Message type mismatch for channel: " + channel);
        }
        registration.addHandler(handler);
        return this;
    }

    /**
     * 注册处理器。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @param handling    处理器处理信息列表
     * @return 当前配置器实例
     */
    public DefaultConfigurator registerHandlers(String channel, Class<?> messageType, List<ChannelHandler> handling) {
        Assert.notNull(messageType, "Message type cannot be null");
        Assert.notEmpty(handling, "Handling cannot be null or empty");

        var registration = registrations.putIfAbsent(channel, new ChannelRegistration(messageType));
        if (registration == null) {
            throw new IllegalStateException("Duplicate handling for channel: " + channel);
        }
        if (messageType != registration.getMessageType()) {
            throw new IllegalStateException("Message type mismatch for channel: " + channel);
        }
        handling.forEach(registration::addHandler);
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
        MessageHandlerFinder.find(this::registerHandlers, types.toArray(Class<?>[]::new));
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
        MessageHandlerFinder.find(this::registerHandlers, types);
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
        MessageHandlerFinder.find(this::registerHandlers, packageNames);
        return this;
    }

    /**
     * 设置默认传输方式。
     *
     * @param defaultTransportSupplier 默认传输方式的提供者
     * @return 当前配置器实例
     */
    public DefaultConfigurator setDefaultTransport(Supplier<String> defaultTransportSupplier) {
        Assert.notNull(defaultTransportSupplier, "Default transport supplier cannot be null");
        this.defaultTransportSupplier = defaultTransportSupplier;
        return this;
    }

    /**
     * 设置是否启用管道行为。
     *
     * @param enablePipelineBehaviorsSupplier 启用管道行为的提供者
     * @return 当前配置器实例
     */
    public DefaultConfigurator setEnablePipelineBehaviors(Supplier<Boolean> enablePipelineBehaviorsSupplier) {
        Assert.notNull(enablePipelineBehaviorsSupplier, "Enable pipeline behaviors cannot be null");
        this.enablePipelineBehaviorsSupplier = enablePipelineBehaviorsSupplier;
        return this;
    }
}
