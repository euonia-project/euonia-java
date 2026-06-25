package com.euonia.bus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.serialization.MessageSerializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;

/**
 * RabbitMQ 消息接收者的抽象基类，封装了与 RabbitMQ 交互的公共逻辑。
 * <p>
 * 提供消息接收/确认事件的监听器管理、消息异步处理以及 AMQP 回复属性构建等公共功能。
 * 子类需实现 {@link #start(String)} 方法以启动具体的消费模式（队列消费、主题订阅或 RPC 执行）。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class RabbitMqRecipient {
    /**
     * RabbitMQ 连接
     */
    protected final Connection connection;
    /**
     * RabbitMQ 总线选项
     */
    protected final RabbitMqBusOptions options;
    /**
     * 处理器上下文，用于将消息分派到已注册的处理器
     */
    protected final HandlerContext handler;
    /**
     * 消息序列化器
     */
    protected final MessageSerializer serializer;
    /**
     * 此接收者处理的消息类型
     */
    protected final Type messageType;
    /**
     * 消息接收事件监听器列表
     */
    private final List<Consumer<MessageReceivedEvent>> messageReceivedListeners = new ArrayList<>();
    /**
     * 消息确认事件监听器列表
     */
    private final List<Consumer<MessageAcknowledgedEvent>> messageAcknowledgedListeners = new ArrayList<>();

    /**
     * 使用必要的依赖构造 RabbitMQ 接收者。
     *
     * @param connection  RabbitMQ 连接
     * @param options     RabbitMQ 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 此接收者处理的消息类型
     */
    protected RabbitMqRecipient(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Type messageType) {
        this.connection = connection;
        this.options = options;
        this.handler = handler;
        this.serializer = serializer;
        this.messageType = messageType;
    }

    /**
     * 注册消息接收事件监听器。当从 RabbitMQ 接收到消息时，监听器将被通知。
     *
     * @param listener 消息接收事件消费者
     */
    public void onMessageReceived(Consumer<MessageReceivedEvent> listener) {
        messageReceivedListeners.add(listener);
    }

    /**
     * 注册消息确认事件监听器。当消息被确认（ack）时，监听器将被通知。
     *
     * @param listener 消息确认事件消费者
     */
    public void onMessageAcknowledged(Consumer<MessageAcknowledgedEvent> listener) {
        messageAcknowledgedListeners.add(listener);
    }

    /**
     * 触发消息接收事件，通知所有已注册的消息接收监听器。
     *
     * @param event 消息接收事件
     */
    protected void raiseMessageReceived(MessageReceivedEvent event) {
        for (Consumer<MessageReceivedEvent> listener : messageReceivedListeners) {
            listener.accept(event);
        }
    }

    /**
     * 触发消息确认事件，通知所有已注册的消息确认监听器。
     *
     * @param event 消息确认事件
     */
    protected void raiseMessageAcknowledged(MessageAcknowledgedEvent event) {
        for (Consumer<MessageAcknowledgedEvent> listener : messageAcknowledgedListeners) {
            listener.accept(event);
        }
    }

    /**
     * 异步处理接收到的路由消息，将其负载分派到处理器上下文。
     * <p>
     * 处理完成后，如果发生错误则调用 {@code context.failure()}，
     * 否则调用 {@code context.response()} 设置响应结果。
     *
     * @param message 路由消息
     * @param context 消息上下文
     * @return 处理完成后的异步结果
     */
    protected CompletableFuture<Object> handleAsync(RoutedMessage<?> message, MessageContext context) {
        return handler.handleAsync(message.getPayload(), context)
                      .whenComplete((result, error) -> {
                          if (error != null) {
                              context.failure(error);
                          } else {
                              context.response(result);
                          }
                      });
    }

    /**
     * 启动接收者，开始监听指定通道的消息。
     *
     * @param group 要监听的通道/组名称
     */
    abstract void start(String group);

    /**
     * 返回接收者的名称，默认为当前类的简单名称。
     *
     * @return 接收者名称
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 创建带有关联 ID 和回复类型的 AMQP 回复属性。
     *
     * @param correlationId 关联 ID，用于将回复与请求匹配
     * @param type          回复类型
     * @return AMQP 基本属性对象
     */
    AMQP.BasicProperties createReplyProperties(String correlationId, RabbitMqReplyType type) {
        return new AMQP.BasicProperties().builder()
                                         .contentType("application/json")
                                         .correlationId(correlationId)
                                         .type(type.getValue())
                                         .build();
    }
}
