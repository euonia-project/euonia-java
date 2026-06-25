package com.euonia.bus;

import java.io.IOException;
import java.lang.reflect.Type;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Executor;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

public final class RabbitMqRequestExecutor extends RabbitMqRecipient implements Executor {

    private Channel channel;

    public RabbitMqRequestExecutor(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Type messageType) {
        super(connection, options, handler, serializer, messageType);
    }

    @Override
    void start(String group) {
        var queuePrefix = StringUtility.collapse(options.getRpcQueuePrefix(), Constants.DEFAULT_QUEUE_NAME_PREFIX);
        var queueName = String.format("%s:%s@%s", queuePrefix, group, options.getSubscriptionId());

        try {
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                RoutedMessage<?> message = serializer.deserialize(new String(delivery.getBody()), messageType);
                var context = new MessageContextBase(message);
                raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                handleAsync(message, context).whenComplete((result, exception) -> {
                    if (delivery.getProperties().getCorrelationId() == null || delivery.getProperties().getReplyTo() == null) {
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
                        type = "empty";
                        data = "";
                    }

                    var replyProperties = new AMQP.BasicProperties.Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .type(type)
                        .build();

                    try {
                        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProperties, data.getBytes());
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                });
            };

            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
