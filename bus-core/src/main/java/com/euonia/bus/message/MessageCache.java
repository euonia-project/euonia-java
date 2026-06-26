package com.euonia.bus.message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.euonia.bus.annotation.Channel;

/**
 * 线程安全的消息类型与通道名称缓存。用于优化消息类型对应通道名称的获取，
 * 减少反射和注解处理的开销。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class MessageCache {
    private static volatile MessageCache instance;
    private static final Object lock = new Object();

    /**
     * 获取 MessageCache 的单例实例。
     *
     * @return MessageCache 的单例实例
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public static MessageCache getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new MessageCache();
                }
            }
        }
        return instance;
    }

    private final ConcurrentMap<Class<?>, String> channels = new ConcurrentHashMap<>();

    /**
     * 获取指定消息类型对应的通道名称。如果通道名称尚未缓存，则从消息类型的
     * 注解中获取并加入缓存。
     *
     * @param messageType 要获取通道名称的消息类型
     * @return 指定消息类型的通道名称
     */
    public String getOrAddChannel(Class<?> messageType) {
        return channels.computeIfAbsent(messageType, key -> {
            var annotation = messageType.getAnnotation(Channel.class);
            return annotation == null ? messageType.getName() : annotation.value();
        });
    }
}
