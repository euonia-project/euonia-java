package com.euonia.bus.consistency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.euonia.bus.MessageEnvelope;

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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public MessageEnvelope<?> getContent() {
        return content;
    }

    public void setContent(MessageEnvelope<?> content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OutboxTransport> getTransports() {
        return transports;
    }

    public void setTransports(List<OutboxTransport> transports) {
        this.transports = transports;
    }

    /**
     * 添加一条传输状态记录。
     *
     * @param transport 传输状态
     */
    public void addTransport(OutboxTransport transport) {
        this.transports.add(transport);
    }

    /**
     * 按传输名称添加传输记录（当前为空实现）。
     *
     * @param transport 传输名称
     */
    public void addTransports(String transport) {
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
