package com.euonia.bus;

import java.time.Instant;

import com.euonia.core.GuidType;
import com.euonia.core.ObjectId;
import com.euonia.utility.Assert;

/**
 * {@link RoutedMessage} 是表示带有路由信息的消息的不可变类。
 * 它实现了 {@link MessageEnvelope} 接口，为通过消息系统路由的消息提供公共属性和方法。
 *
 * @param <T> 消息中包含的负载类型
 * @author damon(zhaorong@outlook.com)
 */
public final class RoutedMessage<T> implements MessageEnvelope<T> {
    /**
     * 元数据中存储类型名称所用的键
     */
    private final static String MESSAGE_TYPE_KEY = "$nerosoft.euonia:message.type";

    /**
     * 消息元数据容器
     */
    private final MessageMetadata metadata = new MessageMetadata();
    /**
     * 消息唯一标识符
     */
    private String messageId;
    /**
     * 关联标识符，用于在请求-响应模式中关联消息
     */
    private String correlationId;
    /**
     * 会话标识符，用于关联同一对话中的多条消息
     */
    private String conversationId;
    /**
     * 请求追踪标识符，用于端到端请求追踪
     */
    private String requestTrackId;
    /**
     * 消息所属的通道名称
     */
    private String channel;
    /**
     * 授权令牌
     */
    private String authorization;
    /**
     * 消息时间戳（Unix 毫秒）
     */
    private long timestamp = Instant.now().toEpochMilli();
    /**
     * 消息负载对象
     */
    private T payload;

    /**
     * 创建一个空的路由消息实例。
     */
    public RoutedMessage() {
    }

    /**
     * 使用负载和通道创建路由消息，消息 ID 通过 {@link ObjectId} 自动生成。
     *
     * @param payload 消息负载
     * @param channel 消息通道
     */
    public RoutedMessage(T payload, String channel) {
        this(payload, channel, ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString());
    }

    /**
     * 使用负载、通道和消息 ID 创建路由消息。
     *
     * @param payload   消息负载，不能为 null
     * @param channel   消息通道
     * @param messageId 消息唯一标识符
     */
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

    @Override
    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取消息元数据容器。
     *
     * @return 消息元数据
     */
    @Override
    public MessageMetadata getMetadata() {
        return metadata;
    }

    /**
     * 根据键获取元数据值。
     *
     * @param key 元数据键
     * @return 元数据值，如果键不存在则返回 null
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * 设置元数据键值对。
     *
     * @param key   元数据键
     * @param value 元数据值
     */
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * 根据键和类型获取元数据值。
     *
     * @param <V>  值的类型
     * @param key  元数据键
     * @param type 值的类型
     * @return 元数据值，如果键不存在或类型不匹配则返回 null
     */
    public <V> V getMetadata(String key, Class<V> type) {
        return metadata.get(key, type);
    }

    /**
     * 获取消息负载。
     *
     * @return 消息负载
     */
    @Override
    public T getPayload() {
        return payload;
    }

    /**
     * 设置消息负载，并自动将负载类名写入类型名称元数据。
     *
     * @param payload 消息负载
     */
    public void setPayload(T payload) {
        this.payload = payload;
        if (payload != null) {
            setTypeName(payload.getClass().getName());
        }
    }

    /**
     * 获取消息类型名称（负载类的完全限定名）。
     *
     * @return 消息类型名称
     */
    @Override
    public String getTypeName() {
        return metadata.get(MESSAGE_TYPE_KEY, String.class);
    }

    /**
     * 设置消息类型名称。
     *
     * @param typeName 类型名称
     */
    public void setTypeName(String typeName) {
        this.metadata.put(MESSAGE_TYPE_KEY, typeName);
    }

    /**
     * 返回消息的字符串表示，格式为 {@code messageId:{typeName}}。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return String.format("%s:{%s}", messageId, getTypeName());
    }
}
