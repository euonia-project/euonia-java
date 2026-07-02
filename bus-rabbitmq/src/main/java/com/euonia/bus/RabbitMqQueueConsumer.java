package com.euonia.bus;

import java.io.IOException;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Consumer;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * RabbitMQ 队列消费者，实现单播消息的消费。
 * <p>
 * 从指定队列消费消息，异步处理后将回复（如有）发送回调用者声明的
 * {@code replyTo} 队列。支持基于 correlationId 的请求-响应匹配。
 *
 * @author damon(zhaorong@outlook.com)
 */
final class RabbitMqQueueConsumer extends RabbitMqRecipient implements Consumer {

    private Channel channel;
    private String recipientTag;

    /**
     * 使用必要的依赖构造队列消费者。
     *
     * @param connection  RabbitMQ 连接
     * @param options     RabbitMQ 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 此消费者处理的消息类型
     */
    public RabbitMqQueueConsumer(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
        super(connection, options, handler, serializer, messageType);
    }

    /**
     * 启动队列消费，声明持久队列并开始监听消息。
     * <p>
     * 收到消息后：
     * <ol>
     *   <li>反序列化消息体</li>
     *   <li>触发消息接收事件</li>
     *   <li>异步处理消息</li>
     *   <li>如果请求包含 correlationId 和 replyTo，将处理结果或异常发送回回复队列</li>
     * </ol>
     *
     * @param channelName 通道组名称，用于生成队列名
     */
    @Override
    void start(String channelName) {
        var queuePrefix = StringUtility.collapse(options.getQueueNamePrefix(), RabbitMqConstants.DEFAULT_QUEUE_NAME_PREFIX);
        var queueName = options.generateQueueName(queuePrefix, channelName);

        try {
            this.channel = connection.createChannel();

            var dlxArgs = declareDeadLetterInfrastructure(this.channel, channelName);
            this.channel.queueDeclare(queueName, true, false, false, dlxArgs);
            this.channel.basicQos(0, options.getPrefetchCount(), false);

            var consumer = new DefaultConsumer(RabbitMqQueueConsumer.this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {

                    MessageEnvelope<?> message = serializer.deserializeEnvelope(new String(body), messageType);
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
                                   RabbitMqQueueConsumer.this.channel.basicPublish("", properties.getReplyTo(), true, replyProperties, data.getBytes());
                                   RabbitMqQueueConsumer.this.channel.basicAck(envelope.getDeliveryTag(), false);
                                   raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                               } catch (IOException exception) {
                                   // 处理发送回复消息时发生的异常
                                   // 可以选择记录日志或其他处理方式
                               }
                           });

                }
            };

            recipientTag = this.channel.basicConsume(queueName, false, consumer);
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
