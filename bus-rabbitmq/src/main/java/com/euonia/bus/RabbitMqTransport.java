package com.euonia.bus;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.contract.Transport;
import com.euonia.bus.exception.MessageDeliverException;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

public class RabbitMqTransport implements Transport {

    private static final Logger LOGGER = Logger.getLogger(RabbitMqTransport.class.getName());

    private final Connection connection;
    private final MessageSerializer serializer;
    private final RabbitMqBusOptions options;
    private final RetryPolicy<Object> retryPolicy;

    public RabbitMqTransport(Connection connection, MessageSerializer serializer, RabbitMqBusOptions options) {
        this.connection = connection;
        this.serializer = serializer;
        this.options = options;
        this.retryPolicy = createRetryPolicy();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public <M> CompletableFuture<Void> publishAsync(RoutedMessage<M> message) {
        try (var channel = connection.createChannel()) {
            var typeName = message.getTypeName();

            var properties = new AMQP.BasicProperties().builder()
                                                       .contentType("application/json")
                                                       .type(typeName)
                                                       .headers(Map.of(MessageHeaders.MESSAGE_TYPE, typeName))
                                                       .build();

            var data = serializer.serialize(message);

            var exchangePrefix = StringUtility.collapse(options.getExchangeNamePrefix(),
                Constants.DEFAULT_EXCHANGE_NAME_PREFIX);
            var exchangeName = String.format("%s:%s", exchangePrefix, message.getChannel());
            var routingKey = String.format("%s@%s", exchangeName,
                message.getMetadata().get(MessageHeaders.ROUTING_KEY));

            return Failsafe.with(retryPolicy).runAsync(() -> {
                channel.exchangeDeclare(exchangeName, "fanout", true);
                channel.basicPublish(exchangeName, routingKey, options.isMandatory(), properties, data.getBytes());
                LOGGER.log(Level.INFO, "Published message to exchange {0}: {1}", new Object[]{exchangeName, data});
            });
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to publish message: {0}", exception.getMessage());
            return CompletableFuture.failedFuture(exception);
        }
    }

    @Override
    public <M> CompletableFuture<Void> sendAsync(RoutedMessage<M> message) {
        return sendAsync(message, Void.class);
    }

    @Override
    public <M, R> CompletableFuture<R> sendAsync(RoutedMessage<M> message, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();

        try (var channel = connection.createChannel()) {
            var requestQueueName = getQueueName(message.getChannel());
            checkQueue(channel, requestQueueName);
            var responseQueueName = channel.queueDeclare();
            var consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    var correlationId = properties.getCorrelationId();

                    if (!Objects.equals(correlationId, message.getCorrelationId())) {
                        return;
                    }

                    if (responseType.equals(Void.class)) {
                        future.complete(null);
                    } else {
                        var responseData = new String(body);
                        LOGGER.log(Level.INFO, "Received response for message {0}: {1}", new Object[]{message.getMessageId(), responseData});

                        switch (properties.getType()) {
                            case "exception":
                                var exception = serializer.deserialize(responseData, RuntimeException.class);
                                future.completeExceptionally(exception);
                                return;
                            case "void":
                                future.complete(null);
                                return;
                            case "message":
                                future.complete(serializer.deserialize(responseData, responseType));
                                return;
                            default:
                                future.complete(null);
                        }
                    }
                }
            };

            var typeName = message.getTypeName();

            var properties = new AMQP.BasicProperties().builder()
                                                       .contentType("application/json")
                                                       .type(typeName)
                                                       .headers(Map.of(MessageHeaders.MESSAGE_TYPE, typeName))
                                                       .correlationId(message.getCorrelationId())
                                                       .build();

            var data = serializer.serialize(message);

            return Failsafe.with(retryPolicy).runAsync(() -> {
                channel.basicPublish("", requestQueueName, options.isMandatory(), properties, data.getBytes());
                channel.basicConsume(responseQueueName.getQueue(), true, consumer);
                LOGGER.log(Level.INFO, "Sent message to queue {0}: {1}", new Object[]{requestQueueName, data});
            }).thenCompose(ignored -> future);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to publish message: {0}", exception.getMessage());
            future.completeExceptionally(exception);
        }

        return future;
    }

    @Override
    public <M, R> CompletableFuture<R> requestAsync(RoutedMessage<M> message) {
        return null;
    }

    private String getQueueName(String channelName) {
        var queuePrefix = StringUtility.collapse(options.getQueueNamePrefix(),
            Constants.DEFAULT_QUEUE_NAME_PREFIX);
        return String.format("%s:%s@%s", queuePrefix, channelName, options.getSubscriptionId());
    }

    /**
     * 检查指定队列是否存在，并且至少有一个消费者在监听。
     *
     * @param channel          the channel to use for checking the queue
     * @param requestQueueName the name of the queue to check
     */
    private void checkQueue(Channel channel, String requestQueueName) {
        try {
            var queueDeclare = channel.queueDeclarePassive(requestQueueName);
            if (queueDeclare == null) {
                throw new MessageDeliverException(String.format("Channel not found in vhost '%s'", options.getVirtualHost()));
            }

            if (queueDeclare.getConsumerCount() < 1) {
                throw new MessageDeliverException(String.format("No consumers for queue '%s' in vhost '%s'", requestQueueName, options.getVirtualHost()));
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to check queue: {0}", exception.getMessage());
            throw new MessageDeliverException(String.format("Failed to check queue '%s' in vhost '%s'", requestQueueName, options.getVirtualHost()), exception);
        }
    }

    private RetryPolicy<Object> createRetryPolicy() {
        return RetryPolicy.builder()
                          .handle(IOException.class)
                          .handle(TimeoutException.class)
                          .withDelay(Duration.ofMillis(options.getRetryDelay()))
                          .withMaxRetries(options.getRetryAttempts())
                          .build();
    }
}
