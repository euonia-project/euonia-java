package com.euonia.bus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.serialization.MessageSerializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * RabbitMQ 消息接收者的抽象基类，封装了与 RabbitMQ 交互的公共逻辑。
 * <p>
 * 提供消息接收/确认事件的监听器管理、消息异步处理以及 AMQP 回复属性构建等公共功能。
 * 子类需实现 {@link #start(String)} 方法以启动具体的消费模式，以及 {@link #stop()} 方法来停止监听并释放资源。
 * <p>
 * 实现 {@link AutoCloseable}，调用 {@link #close()} 将:
 * <ol>
 *   <li>调用 {@link #stop()} 停止消息监听并关闭 Channel</li>
 *   <li>清除所有已注册的事件监听器</li>
 * </ol>
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class RabbitMqRecipient implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(RabbitMqRecipient.class.getName());
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
    protected final Class<?> messageType;
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
    protected RabbitMqRecipient(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
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
     * 启动接收者，开始监听指定通道的消息。
     *
     * @param channelName 要监听的通道名称
     */
    abstract void start(String channelName);

    /**
     * 停止接收者，取消消费者并关闭 Channel。
     * <p>
     * 实现应:
     * <ol>
     *   <li>调用 {@code channel.basicCancel(consumerTag)} 停止投递</li>
     *   <li>调用 {@code channel.close()} 释放 Channel 资源</li>
     * </ol>
     * 此方法可能被多次调用，实现应为幂等的。
     */
    protected abstract void stop();

    /**
     * 关闭此接收者，停止消息监听并清除所有事件监听器。
     * <p>
     * 此方法可安全地多次调用。
     */
    @Override
    public void close() {
        try {
            stop();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, () -> "Error stopping recipient '" + getName() + "': " + e.getMessage());
        }
        messageReceivedListeners.clear();
        messageAcknowledgedListeners.clear();
    }

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

    /**
     * 声明死信交换器和死信队列（如已启用），并返回应传递给主队列声明的
     * AMQP 参数（{@code x-dead-letter-exchange} 和 {@code x-dead-letter-routing-key}）。
     *
     * @param channel     用于声明的通道
     * @param channelName 主通道名称，用于生成 DLX/DLQ 名
     * @return 传递给 {@code channel.queueDeclare} 的参数映射
     * @throws IOException 声明失败时抛出
     */
    Map<String, Object> declareDeadLetterInfrastructure(Channel channel, String channelName) throws IOException {
        if (!options.isDeadLetterEnabled()) {
            return null;
        }

        var dlxName = options.generateDlxExchangeName(channelName);
        var dlqName = options.generateDlqQueueName(channelName);

        channel.exchangeDeclare(dlxName, BuiltinExchangeType.FANOUT, true);
        channel.queueDeclare(dlqName, true, false, false, null);
        channel.queueBind(dlqName, dlxName, RabbitMqConstants.DEFAULT_DLX_ROUTING_KEY);

        return Map.of(
            "x-dead-letter-exchange", dlxName,
            "x-dead-letter-routing-key", RabbitMqConstants.DEFAULT_DLX_ROUTING_KEY
        );
    }
}
