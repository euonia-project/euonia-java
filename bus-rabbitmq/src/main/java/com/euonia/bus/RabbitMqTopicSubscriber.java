package com.euonia.bus;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Subscriber;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.*;

import java.io.IOException;

public final class RabbitMqTopicSubscriber extends RabbitMqRecipient implements Subscriber {

    private Channel channel;

    private final Class<? extends RoutedMessage<?>> messageType;

    public RabbitMqTopicSubscriber(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<? extends RoutedMessage<?>> messageType) {
        super(connection, options, handler, serializer);
        this.messageType = messageType;
    }

    @Override
    void start(String group) {
        try {
            channel = connection.createChannel();

            var exchangePrefix = StringUtility.collapse(options.getExchangeNamePrefix(), Constants.DEFAULT_EXCHANGE_NAME_PREFIX);
            var exchangeName = String.format("%s:%s", exchangePrefix, group);

            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, false);
            var queueName = channel.queueDeclare(String.format("%s@%s", exchangeName, options.getSubscriptionId()), true, false, false, null).getQueue();
            channel.queueBind(queueName, exchangeName, "*");
            var consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {

                    RoutedMessage<?> message = serializer.deserialize(new String(body), messageType);
                    var context = new MessageContextBase(message);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handle(message, context);
                    try {
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
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
