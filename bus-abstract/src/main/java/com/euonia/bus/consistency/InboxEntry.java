package com.euonia.bus.consistency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.euonia.bus.MessageEnvelope;

/**
 * 收件箱（Inbox）条目，用于幂等处理。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class InboxEntry {

    /** 消息唯一标识符 */
    private String messageId;
    /** 消息所属通道 */
    private String channel;
    /** 消息类型名称 */
    private String messageType;
    /** 消息内容（负载） */
    private MessageEnvelope<?> content;
    /** 条目创建时间 */
    private LocalDateTime createdAt;
    /** 处理状态列表 */
    private List<InboxHandle> handles = new ArrayList<>();

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

    public List<InboxHandle> getHandles() {
        return handles;
    }

    public void setHandles(List<InboxHandle> handles) {
        this.handles = handles;
    }

    /**
     * 添加一条处理状态记录。
     *
     * @param handle 处理状态
     */
    public void addHandle(InboxHandle handle) {
        this.handles.add(handle);
    }

    /**
     * 为指定处理器创建并添加一条待处理状态记录。
     *
     * @param handler 处理器名称
     */
    public void addHandle(String handler) {
        var handle = new InboxHandle();
        handle.setHandler(handler);
        handle.setMessageId(messageId);
        this.handles.add(handle);
    }

    /**
     * 返回收件箱条目的字符串表示。
     *
     * @return 格式为 {@code InboxEntry{channel=xxx, messageId=xxx, messageType=xxx}} 的字符串
     */
    @Override
    public String toString() {
        return String.format("InboxEntry{channel=%s, messageId=%s, messageType=%s}", channel, messageId, messageType);
    }
}
