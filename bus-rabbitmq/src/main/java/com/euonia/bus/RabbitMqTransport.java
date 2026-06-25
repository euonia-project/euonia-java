package com.euonia.bus;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.euonia.bus.contract.Transport;
import com.euonia.bus.exception.MessageDeliverException;
import com.euonia.bus.exception.MessageTransportException;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

public final class RabbitMqTransport implements Transport {

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
        try {
            var channel = connection.createChannel();
            var typeName = message.getTypeName();

            var properties = new AMQP.BasicProperties().builder()
                                                       .contentType("application/json")
                                                       .type(typeName)
                                                       .headers(Map.of(MessageHeaders.MESSAGE_TYPE, typeName))
                                                       .build();

            var data = serializer.serialize(message);

            var exchangePrefix = StringUtility.collapse(options.getExchangeNamePrefix(), Constants.DEFAULT_EXCHANGE_NAME_PREFIX);
            var exchangeName = String.format("%s:%s", exchangePrefix, message.getChannel());

            return Failsafe.with(retryPolicy)
                           .runAsync(() -> {
                               channel.exchangeDeclare(exchangeName, "fanout", true);
                               channel.basicPublish(exchangeName, "*", options.isMandatory(), properties, data.getBytes());
                               LOGGER.info(() -> String.format("Message '%s' successfully published to exchange %s", message.getMessageId(), exchangeName));
                           })
                           .whenComplete((v, ex) -> closeChannel(channel));
        } catch (IOException exception) {
            LOGGER.severe(() -> String.format("Message '%s' publish failed: %s", message.getMessageId(), exception.getMessage()));
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

        try {
            var channel = connection.createChannel();
            var queueNamePrefix = StringUtility.collapse(options.getQueueNamePrefix(), Constants.DEFAULT_QUEUE_NAME_PREFIX);
            var requestQueueName = checkQueue(channel, queueNamePrefix, message.getChannel());
            var responseQueue = channel.queueDeclare();
            var responseQueueName = responseQueue.getQueue();

            var typeName = message.getTypeName();

            var properties = new AMQP.BasicProperties().builder()
                                                       .contentType("application/json")
                                                       .headers(Map.of(MessageHeaders.MESSAGE_TYPE, typeName))
                                                       .correlationId(message.getCorrelationId())
                                                       .build();

            var data = serializer.serialize(message);

            // Register the response consumer before publishing so it is ready to receive the reply
            String replyTag = channel.basicConsume(responseQueueName, true, (consumerTag, delivery) -> {
                if (!Objects.equals(delivery.getProperties().getCorrelationId(), message.getCorrelationId())) {
                    return;
                }

                try {
                    if (responseType.equals(Void.class)) {
                        future.complete(null);
                    } else {
                        var responseData = new String(delivery.getBody());
                        LOGGER.info(() -> String.format("Message '%s' Received response: %s", message.getMessageId(), responseData));

                        switch (delivery.getProperties().getType()) {
                            case "exception" -> {
                                var exception = serializer.deserialize(responseData, RuntimeException.class);
                                future.completeExceptionally(exception);
                            }
                            case "void" -> future.complete(null);
                            case "message" -> future.complete(serializer.deserialize(responseData, responseType));
                            default ->
                                future.completeExceptionally(new MessageTransportException("Unknown response type: " + delivery.getProperties().getType()));
                        }
                    }
                } finally {
                    // Cancel the temporary reply consumer once a matching response is received
                    cancelConsumer(channel, consumerTag);
                }
            }, consumerTag -> {
                // If the consumer is canceled unexpectedly before the future completes,
                // signal an error so the caller does not wait indefinitely
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new MessageDeliverException("Consumer canceled before receiving response for message: " + message.getMessageId()));
                }
            });

            // Add a timeout to prevent callers from waiting indefinitely.
            // Close the reply channel when the future completes (success, failure, or timeout).
            var timeout = getResponseTimeout();
            future.orTimeout(timeout, TimeUnit.MILLISECONDS)
                  .whenComplete((r, ex) -> {
                      if (ex instanceof TimeoutException) {
                          LOGGER.warning(() -> String.format("Message '%s' timed out waiting for response", message.getMessageId()));
                      }
                      closeChannel(channel);
                  });

            // Publish the command message (with retry) and cancel the consumer on failure
            Failsafe.with(retryPolicy)
                    .runAsync(() -> {
                        channel.basicPublish("", requestQueueName, options.isMandatory(), properties, data.getBytes());
                        LOGGER.info(() -> String.format("Message '%s' successfully sent to queue %s", message.getMessageId(), requestQueueName));
                    }).exceptionally(ex -> {
                        LOGGER.severe(() -> String.format("Message '%s' publish failed after retries: %s", message.getMessageId(), ex.getMessage()));
                        if (!future.isDone()) {
                            future.completeExceptionally(ex);
                        }
                        cancelConsumer(channel, replyTag);
                        return null;
                    });
        } catch (IOException exception) {
            LOGGER.severe(() -> String.format("Message '%s' publish failed: %s", message.getMessageId(), exception.getMessage()));
            future.completeExceptionally(exception);
        }

        return future;
    }

    @Override
    public <M, R> CompletableFuture<R> callAsync(RoutedMessage<M> message, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();

        try {
            var channel = connection.createChannel();
            var queueNamePrefix = StringUtility.collapse(options.getRpcQueuePrefix(), Constants.DEFAULT_QUEUE_NAME_PREFIX);
            var requestQueueName = checkQueue(channel, queueNamePrefix, message.getChannel());
            var responseQueue = channel.queueDeclare();
            var responseQueueName = responseQueue.getQueue();

            var typeName = message.getTypeName();

            var properties = new AMQP.BasicProperties().builder()
                                                       .contentType("application/json")
                                                       .headers(Map.of(MessageHeaders.MESSAGE_TYPE, typeName))
                                                       .correlationId(message.getCorrelationId())
                                                       .replyTo(responseQueueName)
                                                       .build();

            var data = serializer.serialize(message);

            // Register the response consumer before publishing so it is ready to receive the reply
            String replyTag = channel.basicConsume(responseQueueName, true, (consumerTag, delivery) -> {
                if (!Objects.equals(delivery.getProperties().getCorrelationId(), message.getCorrelationId())) {
                    return;
                }

                var responseData = new String(delivery.getBody());
                LOGGER.info(() -> String.format("Received response for message '%s': %s", message.getMessageId(), responseData));

                try {
                    switch (delivery.getProperties().getType()) {
                        case "exception" -> {
                            var exception = serializer.deserialize(responseData, RuntimeException.class);
                            future.completeExceptionally(exception);
                        }
                        case "void" -> future.complete(null);
                        case "message" -> future.complete(serializer.deserialize(responseData, responseType));
                        default ->
                            future.completeExceptionally(new MessageTransportException("Unknown response type: " + delivery.getProperties().getType()));
                    }
                } finally {
                    // Cancel the temporary reply consumer once a matching response is received
                    cancelConsumer(channel, consumerTag);
                }
            }, consumerTag -> {
                // If the consumer is canceled unexpectedly before the future completes,
                // signal an error so the caller does not wait indefinitely
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new MessageDeliverException("Consumer canceled before receiving response for message: " + message.getMessageId()));
                }
            });

            // Add a timeout to prevent callers from waiting indefinitely.
            // Close the reply channel when the future completes (success, failure, or timeout).
            var timeout = getResponseTimeout();
            future.orTimeout(timeout, TimeUnit.MILLISECONDS)
                  .whenComplete((r, ex) -> {
                      if (ex instanceof TimeoutException) {
                          LOGGER.warning(() -> String.format("Message '%s' timed out waiting for response", message.getMessageId()));
                      }
                      closeChannel(channel);
                  });

            // Publish the request message (with retry) and cancel the consumer on failure
            Failsafe.with(retryPolicy)
                    .runAsync(() -> {
                        channel.basicPublish("", requestQueueName, options.isMandatory(), properties, data.getBytes());
                        LOGGER.info(() -> String.format("Message '%s' successfully sent to queue %s", message.getMessageId(), requestQueueName));
                    })
                    .exceptionally(ex -> {
                        LOGGER.severe(() -> String.format("Message '%s' publish failed after retries: %s", message.getMessageId(), ex.getMessage()));
                        if (!future.isDone()) {
                            future.completeExceptionally(ex);
                        }
                        cancelConsumer(channel, replyTag);
                        return null;
                    });
        } catch (IOException exception) {
            LOGGER.severe(() -> String.format("Message '%s' publish failed: %s", message.getMessageId(), exception.getMessage()));
            future.completeExceptionally(exception);
        }

        return future;
    }

    /**
     * Computes a response timeout based on retry policy parameters.
     * <p>
     * The timeout is calculated as {@code retryDelay * (retryAttempts + 1)} plus a buffer
     * for network round-trip and handler processing time.
     */
    private long getResponseTimeout() {
        // Base: total retry window + 5s buffer for handler processing and network latency
        return options.getRetryDelay() * (options.getRetryAttempts() + 1) + 5_000L;
    }

    /**
     * Safely closes a channel, logging any error without propagating the exception.
     *
     * @param channel the channel to close
     */
    private void closeChannel(Channel channel) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.warning(() -> "Failed to close channel: " + e.getMessage());
        }
    }

    /**
     * Safely cancels a consumer, logging any error without propagating the exception.
     *
     * @param channel     the channel the consumer belongs to
     * @param consumerTag the consumer tag to cancel
     */
    private void cancelConsumer(Channel channel, String consumerTag) {
        try {
            channel.basicCancel(consumerTag);
        } catch (IOException e) {
            LOGGER.warning(() -> "Failed to cancel consumer '" + consumerTag + "': " + e.getMessage());
        }
    }

    /**
     * 检查指定队列是否存在，并且至少有一个消费者在监听。
     *
     * @param channel         the channel to use for checking the queue
     * @param queueNamePrefix the prefix of the queue name to check
     * @param channelName     the name of the channel associated with the queue
     * @return the full queue name if it exists and has consumers
     * @throws MessageDeliverException if the queue does not exist or has no consumers
     */
    private String checkQueue(Channel channel, String queueNamePrefix, String channelName) {
        var requestQueueName = String.format("%s:%s@%s", queueNamePrefix, channelName, options.getSubscriptionId());

        try {
            var queueDeclare = channel.queueDeclarePassive(requestQueueName);
            if (queueDeclare == null) {
                throw new MessageDeliverException(String.format("Channel not found in vhost '%s'", options.getVirtualHost()));
            }

            if (queueDeclare.getConsumerCount() < 1) {
                throw new MessageDeliverException(String.format("No consumers for queue '%s' in vhost '%s'", requestQueueName, options.getVirtualHost()));
            }
        } catch (IOException exception) {
            throw new MessageDeliverException(String.format("Failed to check queue '%s' in vhost '%s'", requestQueueName, options.getVirtualHost()), exception);
        }
        return requestQueueName;
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
