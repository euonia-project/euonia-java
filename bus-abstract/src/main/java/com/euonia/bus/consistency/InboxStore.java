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

    boolean insert(InboxEntry entry);

    void markAsSuccess(String messageId, String handler);

    void markAsFailed(String messageId, String handler, String errorMessage);

    InboxEntry get(String messageId);

    default InboxEntry getAndCache(String messageId) {
        return CACHE.computeIfAbsent(messageId, this::get);
    }

    default void clearCache() {
        CACHE.clear();
    }

    List<InboxHandle> getFailedMessages();
}
