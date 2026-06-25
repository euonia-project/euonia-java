package com.euonia.bus;

import java.io.IOException;
import java.lang.reflect.Type;

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

public final class RabbitMqQueueConsumer extends RabbitMqRecipient implements Consumer {

    private Channel channel;

    public RabbitMqQueueConsumer(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Type messageType) {
        super(connection, options, handler, serializer, messageType);
    }

    @Override
    void start(String group) {
        var queuePrefix = StringUtility.collapse(options.getQueueNamePrefix(), Constants.DEFAULT_QUEUE_NAME_PREFIX);
        var queueName = String.format("%s:%s@%s", queuePrefix, group, options.getSubscriptionId());

        try {
            channel = connection.createChannel();

            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(0, 1, false);

            var consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    RoutedMessage<?> message = serializer.deserialize(new String(body), messageType);
                    var context = new MessageContextBase(message);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handleAsync(message, context).whenComplete((result, exception) -> {
                        if (properties.getCorrelationId().isBlank() || properties.getReplyTo().isBlank()) {
                            return;
                        }

                        String type, data;

                        if (exception != null) {
                            type = "exception";
                            data = serializer.serialize(exception);
                        } else if (result != null) {
                            type = "message";
                            data = serializer.serialize(result);
                        } else {
                            type = "void";
                            data = "";
                        }

                        var replyProperties = new AMQP.BasicProperties.Builder().correlationId(properties.getCorrelationId()).type(type).build();

                        try {
                            channel.basicPublish("", properties.getReplyTo(), true, replyProperties, data.getBytes());
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (IOException e) {
                            // 处理发送回复消息时发生的异常
                            // 可以选择记录日志或其他处理方式
                        }
                        raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                    });

                }
            };

            channel.basicConsume(queueName, false, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
