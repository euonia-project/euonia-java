package com.euonia.bus;

import com.euonia.core.Singleton;
import com.euonia.utility.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

public class ChannelRegistrar {

    private final ConcurrentMap<String, ChannelRegistration> registrations = new ConcurrentHashMap<>();

    private ChannelRegistrar() {
    }

    /**
     * 获取 ChannelRegistrar 的单例实例。
     *
     * @return ChannelRegistrar 的单例实例
     */
    public static ChannelRegistrar instance() {
        return Singleton.get(ChannelRegistrar.class, ChannelRegistrar::new);
    }

    /**
     * 获取已注册的通道处理器列表。
     *
     * @return 已注册的通道处理器列表
     */
    public Map<String, ChannelRegistration> getRegistrations() {
        return Collections.unmodifiableMap(registrations);
    }

    /**
     * 获取指定通道的注册信息。
     *
     * @param channel 通道名称
     * @return 指定通道的注册信息，如果不存在则返回空的 Optional
     */
    public Optional<ChannelRegistration> getRegistration(String channel) {
        return Optional.ofNullable(registrations.get(channel));
    }

    /**
     * 注册一个通道处理器。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @param handler     通道处理器
     * @return ChannelRegistrar 实例
     */
    public ChannelRegistrar register(String channel, Class<?> messageType, ChannelHandler handler) {
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
     * 注册一个通道处理器列表。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @param handling    通道处理器列表
     * @return ChannelRegistrar 实例
     */
    public ChannelRegistrar register(String channel, Class<?> messageType, List<ChannelHandler> handling) {
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
     * 注册一个通道处理器。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @param handler     通道处理器
     * @param <T>         消息类型
     * @param <R>         返回类型
     * @return ChannelRegistrar 实例
     */
    public <T, R> ChannelRegistrar register(String channel, Class<T> messageType, BiFunction<T, MessageContext, R> handler) {
        try {
            var method = LambdaHandler.class.getDeclaredMethod("handle", Object.class, MessageContext.class);
            return register(channel, messageType, new ChannelHandler(LambdaHandler.class, method, new LambdaHandler<>(handler)));
        } catch (NoSuchMethodException e) {
            return this;
        }
    }


}
