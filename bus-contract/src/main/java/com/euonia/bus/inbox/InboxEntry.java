package com.euonia.bus.inbox;

import com.euonia.bus.MessageEnvelope;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 收件箱（Inbox）条目，用于幂等处理。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class InboxEntry {

    private String messageId;
    private String channel;
    private String messageType;
    private MessageEnvelope<?> content;
    private LocalDateTime createdAt;
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

    public void addHandle(InboxHandle handle) {
        this.handles.add(handle);
    }

    public void addHandle(String handler) {
        var handle = new InboxHandle();
        handle.setHandler(handler);
        handle.setMessageId(messageId);
        this.handles.add(handle);
    }

    @Override
    public String toString() {
        return String.format("InboxEntry{channel=%s, messageId=%s, messageType=%s}", channel, messageId, messageType);
    }
}
