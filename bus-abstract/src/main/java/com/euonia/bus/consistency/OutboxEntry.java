package com.euonia.bus.consistency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.euonia.bus.MessageEnvelope;
import com.euonia.core.ArgumentNullException;

/**
 * 代表收件箱（Outbox）中待发送的持久化消息。
 * <p>
 * 业务事务中将领域事件写入 {@link OutboxStore}，由 {@code OutboxPublisher}
 * 异地轮询并以至少一次的方式投递到消息总线。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class OutboxEntry {

    /**
     * 消息唯一标识符
     */
    private String messageId;

    /**
     * 消息所属通道
     */
    private String channel;

    /**
     * 消息类型名称
     */
    private String messageType;

    /**
     * 消息内容（负载）
     */
    private MessageEnvelope<?> content;

    /**
     * 条目创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 关联的传输状态列表
     */
    private List<OutboxTransport> transports = new ArrayList<>();

    /**
     * 获取消息唯一标识符。
     *
     * @return 消息唯一标识符
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * 设置消息唯一标识符。
     *
     * @param messageId 消息唯一标识符
     */
    public void setMessageId(String messageId) {
        ArgumentNullException.throwIfNullOrEmpty(messageId, "messageId");
        this.messageId = messageId;
    }

    /**
     * 获取消息所属通道。
     *
     * @return 消息所属通道
     */
    public String getChannel() {
        return channel;
    }

    /**
     * 设置消息所属通道。
     *
     * @param channel 消息所属通道
     */
    public void setChannel(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        this.channel = channel;
    }

    /**
     * 获取消息类型名称。
     *
     * @return 消息类型名称
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * 设置消息类型名称。
     *
     * @param messageType 消息类型名称
     */
    public void setMessageType(String messageType) {
        ArgumentNullException.throwIfNullOrEmpty(messageType, "messageType");
        this.messageType = messageType;
    }

    /**
     * 获取消息内容（负载）。
     *
     * @return 消息内容（负载）
     */
    public MessageEnvelope<?> getContent() {
        return content;
    }

    /**
     * 设置消息内容（负载）。
     *
     * @param content 消息内容（负载）
     */
    public void setContent(MessageEnvelope<?> content) {
        this.content = content;
    }

    /**
     * 获取条目创建时间。
     *
     * @return 条目创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置条目创建时间。
     *
     * @param createdAt 条目创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        ArgumentNullException.throwIfNull(createdAt, "createdAt");
        this.createdAt = createdAt;
    }

    /**
     * 获取关联的传输状态列表。
     *
     * @return 关联的传输状态列表
     */
    public List<OutboxTransport> getTransports() {
        return transports;
    }

    /**
     * 设置关联的传输状态列表。
     *
     * @param transports 关联的传输状态列表
     */
    public void setTransports(List<OutboxTransport> transports) {
        ArgumentNullException.throwIfNullOrEmpty(transports, "transports");
        this.transports = transports;
    }

    /**
     * 添加一条传输状态记录。
     *
     * @param transport 传输状态
     */
    public void addTransport(OutboxTransport transport) {
        ArgumentNullException.throwIfNull(transport, "transport");
        this.transports.add(transport);
    }

    /**
     * 按传输名称添加传输记录（当前为空实现）。
     *
     * @param transportName 传输名称
     */
    public void addTransport(String transportName) {
        ArgumentNullException.throwIfNullOrEmpty(transportName, "transportName");
        var transportItem = new OutboxTransport();
        transportItem.setName(transportName);
        transportItem.setMessageId(messageId);
        this.transports.add(transportItem);
    }

    /**
     * 返回投递箱条目的字符串表示。
     *
     * @return 格式为 {@code OutboxMessage{messageId=xxx, channel=xxx}} 的字符串
     */
    @Override
    public String toString() {
        return String.format("OutboxMessage{messageId=%s, channel=%s}", messageId, channel);
    }
}
