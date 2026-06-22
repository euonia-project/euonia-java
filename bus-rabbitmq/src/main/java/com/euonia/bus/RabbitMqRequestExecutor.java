package com.euonia.bus;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.event.MessageRepliedEvent;
import com.euonia.bus.recipient.Executor;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public final class RabbitMqRequestExecutor extends RabbitMqRecipient implements Executor {

    private Channel channel;

    private final Class<? extends RoutedMessage<?>> messageType;

    public RabbitMqRequestExecutor(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<? extends RoutedMessage<?>> messageType) {
        super(connection, options, handler, serializer);
        this.messageType = messageType;
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
                CompletableFuture<Object> future = new CompletableFuture<>();

                var message = serializer.deserialize(new String(delivery.getBody()), messageType);
                var context = getMessageContext(message, future);
                raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                handle(message, context);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));

                future.whenComplete((result, exception) -> {
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
                          } catch (Exception e) {
                              throw new RuntimeException(e);
                          }
                      })
                      .join();

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MessageContext getMessageContext(RoutedMessage<?> message, CompletableFuture<Object> future) {
        var context = new MessageContextBase(message);

        context.onReplied(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(MessageRepliedEvent item) {
                future.complete(item.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        });
        return context;
    }
}
