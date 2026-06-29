package com.euonia.bus;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.euonia.bus.dlq.DeadLetterMessage;

/**
 * 内存死信队列，按通道路由存储处理失败的消息。
 * <p>
 * 单例模式，与 {@link InMemoryTransport} 生命周期绑定。
 * 调用 {@link #reset()} 可清空所有死信消息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class InMemoryDeadLetterQueue {

    private static final InMemoryDeadLetterQueue INSTANCE = new InMemoryDeadLetterQueue();

    private final ConcurrentMap<String, List<DeadLetterMessage<?>>> store = new ConcurrentHashMap<>();

    private InMemoryDeadLetterQueue() {
    }

    public static InMemoryDeadLetterQueue getInstance() {
        return INSTANCE;
    }

    /**
     * 将死信消息存储到指定通道。
     *
     * @param channel 消息通道
     * @param message 死信消息
     */
    public void publish(String channel, DeadLetterMessage<?> message) {
        store.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>()).add(message);
    }

    /**
     * 获取指定通道的所有死信消息（不移除）。
     *
     * @param channel 消息通道
     * @return 死信消息列表，可能为空
     */
    public List<DeadLetterMessage<?>> getDeadLetters(String channel) {
        return store.getOrDefault(channel, List.of());
    }

    /**
     * 获取所有通道的死信消息计数。
     *
     * @return 死信消息总数
     */
    public int size() {
        return store.values().stream().mapToInt(List::size).sum();
    }

    /**
     * 清空所有通道的死信消息。
     */
    public void reset() {
        store.clear();
    }
}
