package com.euonia.bus.outbox;

import java.util.List;

/**
 * 投递箱（Outbox）持久化接口，保证领域事件与业务数据在同一事务中写入。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface OutboxStore {

    /**
     * 将消息写入投递箱。通常在同一数据库事务中调用。
     */
    void save(OutboxEntry message);

    /**
     * 获取所有待发送的消息。
     */
    List<OutboxEntry> getPendingMessages();

    /**
     * 标记消息为已发送（发送完成后删除或标记）。
     */
    void markAsSent(String messageId);

    /**
     * 获取待发送消息数量。
     */
    int size();
}
