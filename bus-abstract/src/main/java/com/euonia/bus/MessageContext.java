package com.euonia.bus;

import java.util.Map;

import com.euonia.security.UserPrincipal;

/**
 * 消息上下文接口，提供消息处理过程中所需的上下文信息，包括消息标识、关联标识、会话标识、请求追踪标识、授权信息、用户主体、消息头、元数据以及响应/失败/完成回调。
 * <p>
 * 此接口继承 {@link AutoCloseable}，允许在消息处理完成后释放相关资源。
 *
 * @author damon(zhaorong@outlook)
 */
public interface MessageContext extends AutoCloseable {

    /**
     * 获取当前消息对象。
     *
     * @return 消息对象
     */
    Object getMessage();

    /**
     * 获取消息唯一标识。
     *
     * @return 消息 ID
     */
    String getMessageId();

    /**
     * 设置消息唯一标识。
     *
     * @param messageId 消息 ID
     */
    void setMessageId(String messageId);

    /**
     * 获取关联标识，用于将相关消息关联在一起。
     *
     * @return 关联 ID
     */
    String getCorrelationId();

    /**
     * 设置关联标识。
     *
     * @param correlationId 关联 ID
     */
    void setCorrelationId(String correlationId);

    /**
     * 获取会话标识，用于标识一组相关的消息对话。
     *
     * @return 会话 ID
     */
    String getConversationId();

    /**
     * 设置会话标识。
     *
     * @param conversationId 会话 ID
     */
    void setConversationId(String conversationId);

    /**
     * 获取请求追踪标识，用于分布式链路追踪。
     *
     * @return 请求追踪 ID
     */
    String getRequestTraceId();

    /**
     * 设置请求追踪标识。
     *
     * @param requestTraceId 请求追踪 ID
     */
    void setRequestTraceId(String requestTraceId);

    /**
     * 获取授权信息（如 Bearer token）。
     *
     * @return 授权字符串
     */
    String getAuthorization();

    /**
     * 设置授权信息。
     *
     * @param authorization 授权字符串
     */
    void setAuthorization(String authorization);

    /**
     * 获取当前用户主体信息。
     *
     * @return 用户主体，可能为 {@code null}
     */
    UserPrincipal getUser();

    /**
     * 获取消息头字典。
     *
     * @return 包含所有消息头的不可变映射
     */
    Map<String, String> getHeaders();

    /**
     * 获取消息元数据。
     *
     * @return 消息元数据，可能为 {@code null}
     */
    MessageMetadata getMetadata();

    /**
     * 设置消息元数据。
     *
     * @param metadata 消息元数据
     */
    void setMetadata(MessageMetadata metadata);

    /**
     * 发送响应消息。
     *
     * @param <R>     响应消息类型
     * @param message 响应消息
     */
    <R> void response(R message);

    /**
     * 通知消息处理失败。
     *
     * @param throwable 处理过程中抛出的异常
     */
    void failure(Throwable throwable);

    /**
     * 完成消息处理并发送最终响应。
     *
     * @param message 响应消息
     */
    void complete(Object message);

    /**
     * 完成消息处理并发送最终响应，同时指定处理器类型。
     *
     * @param message     响应消息
     * @param handlerType 处理器类型
     */
    void complete(Object message, Class<?> handlerType);
}
