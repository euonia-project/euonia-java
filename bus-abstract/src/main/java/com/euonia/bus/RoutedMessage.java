package com.euonia.bus;

import java.time.Instant;

import com.euonia.core.GuidType;
import com.euonia.core.ObjectId;
import com.euonia.utility.Assert;

/**
 * RoutedMessage is an abstract class that represents a message with routing information.
 * It implements the MessageEnvelope interface and provides common properties and methods for messages that are routed through a messaging system.
 *
 * @param <T> the type of the payload contained in the message
 */
public final class RoutedMessage<T> implements MessageEnvelope {
    private final static String MESSAGE_TYPE_KEY = "$nerosoft.euonia:message.type";

    private final MessageMetadata metadata = new MessageMetadata();
    private String messageId;
    private String correlationId;
    private String conversationId;
    private String requestTrackId;
    private String channel;
    private String authorization;
    private long timestamp = Instant.now().toEpochMilli();
    private T payload;

    public RoutedMessage() {
    }

    public RoutedMessage(T payload, String channel) {
        this(payload, channel, ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString());
    }

    public RoutedMessage(T payload, String channel, String messageId) {
        Assert.notNull(payload, "payload should not be null");
        setPayload(payload);
        setChannel(channel);
        setMessageId(messageId);
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String getRequestTrackId() {
        return requestTrackId;
    }

    public void setRequestTrackId(String requestTrackId) {
        this.requestTrackId = requestTrackId;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MessageMetadata getMetadata() {
        return metadata;
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public <V> V getMetadata(String key, Class<V> type) {
        return metadata.get(key, type);
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
        if (payload != null) {
            setTypeName(payload.getClass().getName());
        }
    }

    public String getTypeName() {
        return metadata.get(MESSAGE_TYPE_KEY, String.class);
    }

    public void setTypeName(String typeName) {
        this.metadata.put(MESSAGE_TYPE_KEY, typeName);
    }

    @Override
    public String toString() {
        return String.format("%s:{%s}", messageId, getTypeName());
    }
}
