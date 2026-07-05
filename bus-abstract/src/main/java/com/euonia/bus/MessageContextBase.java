package com.euonia.bus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

import com.euonia.bus.event.MessageHandledEvent;
import com.euonia.bus.event.MessageRepliedEvent;
import com.euonia.security.UserPrincipal;

/**
 * {@link MessageContext} 的内置实现。
 * <p>
 * 提供线程安全的消息处理上下文，支持基于事件的已回复、已完成和失败状态
 * 通知。在调用 {@link #close()} 时，上下文会自动调用 {@link #complete(Object)}
 * 并传入原始消息，以标志消息处理结束。
 *
 * @author damon(zhaorong@outlook.com)
 * @see MessageContext
 */
public final class MessageContextBase implements MessageContext {

    /** 消息负载。 */
    private final Object message;
    /** 消息头字典。 */
    private final Map<String, String> headers = new java.util.HashMap<>();
    /** 当前用户主体。 */
    private final UserPrincipal user;
    /** 消息元数据。 */
    private MessageMetadata metadata;
    /** 是否已释放。 */
    private boolean disposed;

    /** 消息已回复事件的发布者。 */
    private final SubmissionPublisher<MessageRepliedEvent> publisher = new SubmissionPublisher<>();
    /** 消息处理完成事件的消费者列表。 */
    private final List<Consumer<MessageHandledEvent>> onCompletedConsumers = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * 使用指定的消息初始化新实例。
     *
     * @param message 消息负载
     */
    public MessageContextBase(Object message) {
        this.message = message;
        this.user = null;
    }

    /**
     * 从路由消息信封初始化新实例，将所有信封元数据（ID、授权信息）复制到
     * 上下文中。
     *
     * @param pack 路由消息信封
     */
    public MessageContextBase(MessageEnvelope<?> pack) {
        this.message = pack.getPayload();
        this.user = null; // RoutedMessage does not yet carry user information
        headers.put(MessageHeaders.MESSAGE_ID, pack.getMessageId());
        headers.put(MessageHeaders.CORRELATION_ID, pack.getCorrelationId());
        headers.put(MessageHeaders.CONVERSATION_ID, pack.getConversationId());
        headers.put(MessageHeaders.REQUEST_TRACE_ID, pack.getRequestTrackId());
        headers.put(MessageHeaders.AUTHORIZATION, pack.getAuthorization());
        this.metadata = pack.getMetadata();
    }

    // ---- 监听器注册（供总线基础设施使用） ----

    /**
     * 注册消息已回复事件的订阅者。
     *
     * @param <R>        响应类型
     * @param subscriber 消息已回复事件的订阅者
     */
    public <R> void onReplied(Flow.Subscriber<? super MessageRepliedEvent> subscriber) {
        publisher.subscribe(subscriber);
    }

    /**
     * 添加消息处理完成事件的消费者。
     *
     * @param consumer 消息处理完成事件的消费者
     */
    public void addCompletedSubscriber(Consumer<MessageHandledEvent> consumer) {
        onCompletedConsumers.add(consumer);
    }

    /**
     * 移除消息处理完成事件的消费者。
     *
     * @param consumer 要移除的消费者
     */
    public void removeCompletedSubscriber(Consumer<MessageHandledEvent> consumer) {
        onCompletedConsumers.remove(consumer);
    }

    // ---- MessageContext implementation ----

    @Override
    public Object getMessage() {
        return message;
    }

    @Override
    public String getMessageId() {
        return headers.getOrDefault(MessageHeaders.MESSAGE_ID, null);
    }

    @Override
    public void setMessageId(String messageId) {
        headers.put(MessageHeaders.MESSAGE_ID, messageId);
    }

    @Override
    public String getCorrelationId() {
        return headers.getOrDefault(MessageHeaders.CORRELATION_ID, null);
    }

    @Override
    public void setCorrelationId(String correlationId) {
        headers.put(MessageHeaders.CORRELATION_ID, correlationId);
    }

    @Override
    public String getConversationId() {
        return headers.getOrDefault(MessageHeaders.CONVERSATION_ID, null);
    }

    @Override
    public void setConversationId(String conversationId) {
        headers.put(MessageHeaders.CONVERSATION_ID, conversationId);
    }

    @Override
    public String getRequestTraceId() {
        return headers.getOrDefault(MessageHeaders.REQUEST_TRACE_ID, null);
    }

    @Override
    public void setRequestTraceId(String requestTraceId) {
        headers.put(MessageHeaders.REQUEST_TRACE_ID, requestTraceId);
    }

    @Override
    public String getAuthorization() {
        return headers.getOrDefault(MessageHeaders.AUTHORIZATION, null);
    }

    @Override
    public void setAuthorization(String authorization) {
        headers.put(MessageHeaders.AUTHORIZATION, authorization);
    }

    @Override
    public UserPrincipal getUser() {
        return user;
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public MessageMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(MessageMetadata metadata) {
        this.metadata = metadata;
    }

    // ---- 生命周期方法 ----

    /**
     * 触发已回复事件，通知监听器消息已使用给定的响应进行了回复。
     *
     * @param message 响应消息
     */
    @Override
    public <R> void response(R message) {
        MessageRepliedEvent args = new MessageRepliedEvent(null, message);
        publisher.submit(args);
    }

    /**
     * 触发失败事件，通知监听器消息处理遇到错误。
     *
     * @param throwable 导致失败的异常
     */
    @Override
    public void failure(Throwable throwable) {
        publisher.closeExceptionally(throwable);
    }

    /**
     * 触发已完成事件，通知监听器消息已完全处理。
     *
     * @param message 已处理的消息
     */
    @Override
    public void complete(Object message) {
        complete(message, null);
    }

    /**
     * 触发已完成事件，同时携带处理器类型信息。
     *
     * @param message     已处理的消息
     * @param handlerType 处理该消息的处理器类型
     */
    @Override
    public void complete(Object message, Class<?> handlerType) {
        MessageHandledEvent args = new MessageHandledEvent(message);
        if (handlerType != null) {
            args.setHandlerType(handlerType);
        }
        for (Consumer<MessageHandledEvent> consumer : onCompletedConsumers) {
            consumer.accept(args);
        }
    }

    /**
     * 释放上下文，使用原始消息触发已完成事件并清理所有已注册的监听器。
     * <p>
     * 此方法对应 .NET 的 dispose 模式：关闭时，上下文发出消息处理完成的
     * 信号并释放所有事件处理器。
     */
    @Override
    public void close() {
        if (disposed) {
            return;
        }
        disposed = true;

        // Signal completion with the original message
        complete(message);
    }
}
