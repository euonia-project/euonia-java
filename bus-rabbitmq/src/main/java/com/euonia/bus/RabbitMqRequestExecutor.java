package com.euonia.bus;

import java.io.IOException;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Executor;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

/**
 * RabbitMQ RPC 请求执行器，实现请求-响应模式的消息消费。
 * <p>
 * 从 RPC 队列消费消息，异步处理后将结果（成功响应或异常）通过
 * 调用者声明的 {@code replyTo} 队列返回。与 {@link RabbitMqQueueConsumer}
 * 相比，此执行器使用 {@link DeliverCallback} 而非 {@code DefaultConsumer}。
 *
 * @author damon(zhaorong@outlook.com)
 */
final class RabbitMqRequestExecutor extends RabbitMqRecipient implements Executor {

    private Channel channel;
    private String recipientTag;

    /**
     * 使用必要的依赖构造 RPC 请求执行器。
     *
     * @param connection  RabbitMQ 连接
     * @param options     RabbitMQ 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 此执行器处理的消息类型
     */
    public RabbitMqRequestExecutor(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
        super(connection, options, handler, serializer, messageType);
    }

    /**
     * 启动 RPC 请求执行，声明持久队列并开始监听消息。
     * <p>
     * 收到消息后：
     * <ol>
     *   <li>反序列化消息体</li>
     *   <li>触发消息接收事件</li>
     *   <li>异步处理消息</li>
     *   <li>将处理结果（成功响应或异常）通过 replyTo 队列返回给调用者</li>
     * </ol>
     *
     * @param channelName 通道组名称，用于生成 RPC 队列名
     */
    @Override
    void start(String channelName) {
        var queuePrefix = StringUtility.collapse(options.getRpcQueuePrefix(), RabbitMqConstants.DEFAULT_QUEUE_NAME_PREFIX);
        var queueName = options.generateQueueName(queuePrefix, channelName);

        try {
            this.channel = connection.createChannel();
            var dlxArgs = declareDeadLetterInfrastructure(this.channel, channelName);
            this.channel.queueDeclare(queueName, true, false, false, dlxArgs);
            this.channel.basicQos(options.getPrefetchCount());

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                var properties = delivery.getProperties();

                MessageEnvelope<?> message = serializer.deserializeEnvelope(new String(delivery.getBody()), messageType);
                var context = new MessageContextBase(message);
                raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                handler.handleAsync(channelName, getName(), message, context)
                       .whenComplete((result, error) -> {
                           if (StringUtility.isNullOrBlank(properties.getCorrelationId()) || StringUtility.isNullOrBlank(properties.getReplyTo())) {
                               return;
                           }
                           try {
                               RabbitMqReplyType type;
                               String data;

                               if (error != null) {
                                   type = RabbitMqReplyType.EXCEPTION;
                                   data = serializer.serialize(error);
                               } else if (result != null) {
                                   type = RabbitMqReplyType.MESSAGE;
                                   data = serializer.serialize(result);
                               } else {
                                   type = RabbitMqReplyType.EMPTY;
                                   data = "";
                               }

                               var replyProperties = createReplyProperties(properties.getCorrelationId(), type);

                               this.channel.basicPublish("", properties.getReplyTo(), replyProperties, data.getBytes());
                               this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                               raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                           } catch (IOException exception) {
                               throw new RuntimeException(exception);
                           }
                       });
            };

            recipientTag = this.channel.basicConsume(queueName, false, deliverCallback, ct -> {
            });
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void stop() {
        if (channel != null && channel.isOpen()) {
            try {
                if (recipientTag != null) {
                    channel.basicCancel(recipientTag);
                }
                channel.close();
            } catch (IOException | java.util.concurrent.TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
