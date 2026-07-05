package com.euonia.bus.outbox;

import java.time.Instant;

import com.euonia.bus.MessageMetadata;
import com.euonia.core.GuidType;
import com.euonia.core.ObjectId;

/**
 * 代表投递箱（Outbox）中待发送的持久化消息。
 * <p>
 * 业务事务中将领域事件写入 {@link OutboxStore}，由 {@code OutboxPublisher}
 * 异地轮询并以至少一次的方式投递到消息总线。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class OutboxEntry {

    private final String id;
    private final Object payload;
    private final String channel;
    private final MessageMetadata metadata;
    private final long createdAt;

    public OutboxEntry(Object payload, String channel) {
        this(payload, channel, null);
    }

    public OutboxEntry(Object payload, String channel, MessageMetadata metadata) {
        this.id = ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString();
        this.payload = payload;
        this.channel = channel;
        this.metadata = metadata != null ? metadata : new MessageMetadata();
        this.createdAt = Instant.now().toEpochMilli();
    }

    public String getId() { return id; }

    public Object getPayload() { return payload; }

    public String getChannel() { return channel; }

    public MessageMetadata getMetadata() { return metadata; }

    public long getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("OutboxMessage{id=%s, channel=%s}", id, channel);
    }
}
