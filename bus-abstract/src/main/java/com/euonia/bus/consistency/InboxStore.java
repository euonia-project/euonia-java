package com.euonia.bus.consistency;

import com.euonia.bus.MessageEnvelope;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 收件箱（Inbox）持久化接口，用于消息去重（幂等性）。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface InboxStore {
    ConcurrentMap<String, InboxEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * 插入一条消息到收件箱。
     *
     * @param channel  消息通道
     * @param message  消息内容
     * @param handlers 处理器列表
     * @param <T>      消息类型
     * @return 插入是否成功
     */
    default <T> boolean insert(String channel, MessageEnvelope<T> message, List<String> handlers) {
        var entry = new InboxEntry();
        entry.setMessageId(message.getMessageId());
        entry.setChannel(channel);
        entry.setContent(message);
        entry.setMessageType(message.getPayload().getClass().getName());
        entry.setCreatedAt(java.time.LocalDateTime.now());
        handlers.forEach(handler -> {
            var handle = new InboxHandle();
            handle.setHandler(handler);
            handle.setStatus(InboxHandle.Status.PENDING.getValue());
            entry.getHandles().add(handle);
        });
        return insert(entry);
    }

    /**
     * 插入一条消息到收件箱。
     *
     * @param entry 消息条目
     * @return 插入是否成功
     */
    boolean insert(InboxEntry entry);

    /**
     * 标记消息处理成功。
     *
     * @param messageId 消息ID
     * @param handler   处理器
     */
    void markAsSuccess(String messageId, String handler);

    /**
     * 标记消息处理失败。
     *
     * @param messageId    消息ID
     * @param handler      处理器
     * @param errorMessage 错误信息
     */
    void markAsFailed(String messageId, String handler, String errorMessage);

    /**
     * 获取收件箱条目。
     *
     * @param messageId 消息ID
     * @return 收件箱条目
     */
    InboxEntry get(String messageId);

    /**
     * 获取收件箱条目并缓存。
     *
     * @param messageId 消息ID
     * @return 收件箱条目
     */
    default InboxEntry getAndCache(String messageId) {
        return CACHE.computeIfAbsent(messageId, this::get);
    }

    /**
     * 清除缓存。
     */
    default void clearCache() {
        CACHE.clear();
    }

    /**
     * 获取处理失败的消息列表。
     *
     * @return 处理失败的消息列表
     */
    List<InboxHandle> getFailedMessages();
}
