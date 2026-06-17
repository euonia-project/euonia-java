package com.euonia.bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 单例内存消息分发器，用于将 {@link MessagePack} 对象按通道路由到已注册的处理器。
 * <p>
 * 此类替代了原始 {@code InMemoryTransport} 实现中使用的 .NET
 * {@code StrongReferenceMessenger} / {@code WeakReferenceMessenger} 模式。
 * <p>
 * 处理器以强引用方式存储——调用者负责在不再需要处理器时取消注册。
 *
 * @author damon(zhaorong@outlook)
 */
public final class InMemoryMessageDispatcher {

    private static final InMemoryMessageDispatcher INSTANCE = new InMemoryMessageDispatcher();

    private final Map<String, List<Consumer<MessagePack>>> channelHandlers = new ConcurrentHashMap<>();

    private InMemoryMessageDispatcher() {
    }

    /**
     * 获取单例分发器实例。
     *
     * @return 分发器实例
     */
    public static InMemoryMessageDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * 为指定通道注册一个处理器。
     *
     * @param channel 消息通道
     * @param handler 当消息被分发时要调用的处理器
     */
    public void register(String channel, Consumer<MessagePack> handler) {
        channelHandlers.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * 从指定通道取消注册一个处理器。
     *
     * @param channel 消息通道
     * @param handler 要移除的处理器
     */
    public void unregister(String channel, Consumer<MessagePack> handler) {
        List<Consumer<MessagePack>> handlers = channelHandlers.get(channel);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    /**
     * 将消息包分发到该消息对应通道的所有已注册处理器。
     * <p>
     * 每个处理器都会被同步调用。如果处理器抛出异常，该异常会传播给调用者。
     *
     * @param pack 要分发的消息包
     */
    public void dispatch(MessagePack pack) {
        String channel = pack.getMessage().getChannel();
        List<Consumer<MessagePack>> handlers = channelHandlers.get(channel);
        if (handlers != null) {
            for (Consumer<MessagePack> handler : handlers) {
                handler.accept(pack);
            }
        }
    }

    /**
     * 移除所有通道的所有已注册处理器。
     */
    public void reset() {
        channelHandlers.clear();
    }
}
