package com.euonia.bus;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.event.MessageRepliedEvent;
import com.euonia.bus.recipient.Consumer;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public final class RabbitMqQueueConsumer extends RabbitMqRecipient implements Consumer {

    private Channel channel;

    private final Class<? extends RoutedMessage<?>> messageType;

    public RabbitMqQueueConsumer(Connection connection, RabbitMqBusOptions options, MessageSerializer serializer, Class<? extends RoutedMessage<?>> messageType) {
        super(connection, options, serializer);
        this.messageType = messageType;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    protected void handle(String channel, Object message, MessageContext context) {

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
                    CompletableFuture<Object> future = new CompletableFuture<>();

                    var message = serializer.deserialize(new String(body), messageType);
                    var context = getMessageContext(message, future);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handle(message.getChannel(), message.getPayload(), context);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));

                    future.whenComplete((result, exception) -> {
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

                              var replyProperties = new AMQP.BasicProperties.Builder()
                                  .correlationId(properties.getCorrelationId())
                                  .replyTo(properties.getReplyTo())
                                  .type(type)
                                  .build();

                              try {
                                  channel.basicPublish("", properties.getReplyTo(), true, replyProperties, data.getBytes());
                              } catch (IOException e) {
                                  // 处理发送回复消息时发生的异常
                                  // 可以选择记录日志或其他处理方式
                              }
                          })
                          .join();

                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };

            channel.basicConsume(queueName, false, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MessageContext getMessageContext(RoutedMessage<?> message, CompletableFuture<Object> future) {
        var context = new MessageContextBase(message);

        context.onReplied(new Flow.Subscriber<MessageRepliedEvent>() {
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
