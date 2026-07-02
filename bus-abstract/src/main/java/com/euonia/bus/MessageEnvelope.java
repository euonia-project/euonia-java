package com.euonia.bus;

/**
 * {@link MessageEnvelope} 定义了通过总线发送的消息的元数据接口。
 * <p>
 * 提供消息路由和追踪所需的核心标识信息，包括消息 ID、关联 ID、
 * 会话 ID、请求追踪 ID 和通道名称。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface MessageEnvelope<T> {
    /**
     * 获取消息唯一标识符。
     *
     * @return 消息 ID
     */
    String getMessageId();

    /**
     * 获取关联标识符，用于在请求-响应模式中关联消息。
     *
     * @return 关联 ID
     */
    String getCorrelationId();

    /**
     * 获取会话标识符，用于关联同一对话中的多条消息。
     *
     * @return 会话 ID
     */
    String getConversationId();

    /**
     * 获取请求追踪标识符，用于端到端请求追踪。
     *
     * @return 请求追踪 ID
     */
    String getRequestTrackId();

    /**
     * 获取消息所属的通道名称。
     *
     * @return 通道名称
     */
    String getChannel();

    /**
     * 获取消息的授权令牌，用于验证消息发送者的身份和权限。
     *
     * @return 授权令牌
     */
    String getAuthorization();

    /**
     * 获取消息的时间戳，表示消息创建的时间，通常以 Unix 毫秒表示。
     *
     * @return 消息时间戳
     */
    long getTimestamp();

    /**
     * 获取消息负载对象。
     *
     * @return 消息负载
     */
    T getPayload();

    /**
     * 获取消息类型名称，通常用于在消息系统中标识消息的类型。
     *
     * @return 消息类型名称
     */
    String getTypeName();

    /**
     * 获取消息的元数据对象，包含消息的附加信息和属性。
     *
     * @return 消息元数据
     */
    MessageMetadata getMetadata();
}
