package com.euonia.bus.consistency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.euonia.bus.MessageEnvelope;
import com.euonia.core.ArgumentNullException;

/**
 * 收件箱（Inbox）条目，用于幂等处理。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class InboxEntry {

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
     * 处理状态列表
     */
    private List<InboxHandler> handlers = new ArrayList<>();

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
        ArgumentNullException.throwIfNull(content, "content");
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
     * 获取处理状态列表。
     *
     * @return 处理状态列表
     */
    public List<InboxHandler> getHandlers() {
        return handlers;
    }

    /**
     * 设置处理状态列表。
     *
     * @param handlers 处理状态列表
     */
    public void setHandlers(List<InboxHandler> handlers) {
        ArgumentNullException.throwIfNull(handlers, "handlers");
        this.handlers = handlers;
    }

    /**
     * 添加一条处理状态记录。
     *
     * @param handle 处理状态
     */
    public void addHandler(InboxHandler handle) {
        ArgumentNullException.throwIfNull(handle, "handle");
        this.handlers.add(handle);
    }

    /**
     * 为指定处理器创建并添加一条待处理状态记录。
     *
     * @param handlerName 处理器名称
     */
    public void addHandler(String handlerName) {
        ArgumentNullException.throwIfNullOrEmpty(handlerName, "handlerName");
        var handle = new InboxHandler();
        handle.setName(handlerName);
        handle.setMessageId(messageId);
        this.handlers.add(handle);
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
