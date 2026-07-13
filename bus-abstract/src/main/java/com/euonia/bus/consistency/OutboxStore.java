package com.euonia.bus.consistency;

import com.euonia.bus.MessageEnvelope;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 发件箱（Outbox）持久化接口，保证领域事件与业务数据在同一事务中写入。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface OutboxStore {

    ConcurrentMap<String, OutboxEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * 插入一条消息到发件箱。
     *
     * @param message    消息内容
     * @param transports 投递方式列表
     * @return 插入是否成功
     */
    default boolean insert(MessageEnvelope<?> message, List<String> transports) {
        var entry = new OutboxEntry();
        entry.setMessageId(message.getMessageId());
        entry.setChannel(message.getChannel());
        entry.setContent(message);
        entry.setMessageType(message.getPayload().getClass().getName());
        entry.setCreatedAt(java.time.LocalDateTime.now());
        transports.forEach(transport -> {
            var outboxTransport = new OutboxTransport();
            outboxTransport.setTransport(transport);
            outboxTransport.setStatus(OutboxTransport.Status.PENDING.getValue());
            entry.addTransport(outboxTransport);
        });
        return insert(entry);
    }

    /**
     * 插入一条消息到发件箱。
     *
     * @param entry 消息条目
     * @return 插入是否成功
     */
    boolean insert(OutboxEntry entry);

    /**
     * 标记消息投递成功。
     *
     * @param messageId 消息ID
     * @param transport 投递方式
     */
    void markAsSuccess(String messageId, String transport);

    /**
     * 标记消息投递失败。
     *
     * @param messageId    消息ID
     * @param transport    投递方式
     * @param errorMessage 错误信息
     */
    void markAsFailed(String messageId, String transport, String errorMessage);

    /**
     * 获取发件箱条目。
     *
     * @param messageId 消息ID
     * @return 发件箱条目
     */
    OutboxEntry get(String messageId);

    /**
     * 获取发件箱条目并缓存。
     *
     * @param messageId 消息ID
     * @return 发件箱条目
     */
    default OutboxEntry getAndCache(String messageId) {
        return CACHE.computeIfAbsent(messageId, this::get);
    }

    /**
     * 清除缓存。
     */
    default void clearCache() {
        CACHE.clear();
    }

    /**
     * 获取投递失败的消息列表。
     *
     * @return 投递失败的消息列表
     */
    List<OutboxTransport> getFailedMessages();
}
