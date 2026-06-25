package com.euonia.bus;

import java.io.IOException;
import java.lang.reflect.Type;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Subscriber;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public final class RabbitMqTopicSubscriber extends RabbitMqRecipient implements Subscriber {

    private Channel channel;

    public RabbitMqTopicSubscriber(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Type messageType) {
        super(connection, options, handler, serializer, messageType);
    }

    @Override
    void start(String group) {
        try {
            channel = connection.createChannel();

            var exchangePrefix = StringUtility.collapse(options.getExchangeNamePrefix(), Constants.DEFAULT_EXCHANGE_NAME_PREFIX);
            var exchangeName = String.format("%s:%s", exchangePrefix, group);

            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
            var queueName = channel.queueDeclare(String.format("%s@%s", exchangeName, options.getSubscriptionId()), true, false, false, null).getQueue();
            channel.queueBind(queueName, exchangeName, "*");
            var consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {

                    RoutedMessage<?> message = serializer.deserialize(new String(body), messageType);
                    var context = new MessageContextBase(message);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handleAsync(message, context).whenComplete((result, exception) -> {
                        try {
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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

    @Override
    public void close() {
        super.close();
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                // Log the exception or handle it as needed
            }
        }
    }
}
