package com.euonia.bus;

/**
 * 消息头部常量定义，包含消息传输中常用的各种头部键名。
 * <p>
 * 这些常量用于在消息传输层（如 AMQP、Kafka）中设置和读取消息的元数据信息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageHeaders {
    /**
     * 会话 ID
     */
    public static final String CONVERSATION_ID = "x-conversation-id";
    /**
     * 请求追踪 ID
     */
    public static final String REQUEST_TRACE_ID = "x-request-trace-id";
    /**
     * 授权令牌
     */
    public static final String AUTHORIZATION = "x-authorization";

    /**
     * 关联 ID
     */
    public static final String CORRELATION_ID = "x-correlation-id";
    /**
     * 消息 ID
     */
    public static final String MESSAGE_ID = "x-message-id";
    /**
     * 消息类型
     */
    public static final String MESSAGE_TYPE = "x-message-type";
    /**
     * 内容类型
     */
    public static final String CONTENT_TYPE = "x-content-type";
    /**
     * 内容编码
     */
    public static final String CONTENT_ENCODING = "x-content-encoding";
    /**
     * 投递模式
     */
    public static final String DELIVERY_MODE = "x-delivery-mode";
    /**
     * 时间戳
     */
    public static final String TIMESTAMP = "x-timestamp";
    /**
     * 优先级
     */
    public static final String PRIORITY = "x-priority";
    /**
     * 过期时间
     */
    public static final String EXPIRATION = "x-expiration";
    /**
     * 回复地址
     */
    public static final String REPLY_TO = "x-reply-to";
    /**
     * 类型
     */
    public static final String TYPE = "x-type";
    /**
     * 用户 ID
     */
    public static final String USER_ID = "x-user-id";
    /**
     * 路由键
     */
    public static final String ROUTING_KEY = "x-routing-key";
}
