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
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

/**
 * RabbitMQ 传输实现，负责通过 RabbitMQ 代理发布、发送和调用消息。
 * <p>
 * 支持三种消息发送模式：
 * <ul>
 *   <li>{@link #publishAsync(RoutedMessage)} —— 多播发布到扇出交换器</li>
 *   <li>{@link #sendAsync(RoutedMessage, Class)} —— 单播发送到指定队列并等待响应</li>
 *   <li>{@link #callAsync(RoutedMessage, Class)} —— RPC 调用并等待响应</li>
 * </ul>
 * 内置基于 Failsafe 的重试策略，处理 IOException 和 TimeoutException。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RabbitMqTransport implements Transport {

    private static final Logger LOGGER = Logger.getLogger(RabbitMqTransport.class.getName());

    /**
     * RabbitMQ 连接
     */
    private final Connection connection;
    /**
     * 消息序列化器
     */
    private final MessageSerializer serializer;
    /**
     * RabbitMQ 总线选项
     */
    private final RabbitMqBusOptions options;
    /**
     * 基于 Failsafe 的重试策略
     */
    private final RetryPolicy<Object> retryPolicy;

    /**
     * 使用必要的依赖构造 RabbitMQ 传输实例。
     *
     * @param connection RabbitMQ 连接
     * @param serializer 消息序列化器
     * @param options    RabbitMQ 总线选项
     */
    public RabbitMqTransport(Connection connection, MessageSerializer serializer, RabbitMqBusOptions options) {
        this.connection = connection;
        this.serializer = serializer;
        this.options = options;
        this.retryPolicy = createRetryPolicy();
    }

    /**
     * 返回传输名称。
     *
     * @return 当前类的简单名称
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 以多播模式发布消息到扇出（fanout）交换器。
     * <p>
     * 交换器名称格式为 {@code {exchangePrefix}:{channel}}。
     * 发布操作受重试策略保护。
     *
     * @param <M>     消息类型
     * @param message 要发布的路由消息
     * @return 发布完成后的异步结果
     */
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
                               channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
                               channel.basicPublish(exchangeName, "*", options.isMandatory(), properties, data.getBytes());
                               LOGGER.info(() -> String.format("Message '%s' successfully published to exchange %s", message.getMessageId(), exchangeName));
                           })
                           .whenComplete((v, ex) -> closeChannel(channel));
        } catch (IOException exception) {
            LOGGER.severe(() -> String.format("Message '%s' publish failed: %s", message.getMessageId(), exception.getMessage()));
            return CompletableFuture.failedFuture(exception);
        }
    }

    /**
     * 以单播模式发送消息到指定队列，不等待响应。
     *
     * @param <M>     消息类型
     * @param message 要发送的路由消息
     * @return 发送完成后的异步结果
     */
    @Override
    public <M> CompletableFuture<Void> sendAsync(RoutedMessage<M> message) {
        return sendAsync(message, Void.class);
    }

    /**
     * 以单播模式发送消息到指定队列，并等待指定类型的响应。
     * <p>
     * 该方法创建一个临时回复队列，发布消息后等待匹配 correlationId 的响应。
     * 内置超时保护和取消回调处理。
     *
     * @param <M>          消息类型
     * @param <R>          响应类型
     * @param message      要发送的路由消息
     * @param responseType 期望的响应类型（{@code Void.class} 表示不期望响应）
     * @return 包含响应的异步结果
     */
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

            // 在发布前注册响应消费者，确保准备好接收回复
            String replyTag = channel.basicConsume(responseQueueName, true, (consumerTag, delivery) -> {

                var repliedProperties = delivery.getProperties();

                if (!Objects.equals(repliedProperties.getCorrelationId(), message.getCorrelationId())) {
                    return;
                }

                try {
                    if (responseType.equals(Void.class)) {
                        future.complete(null);
                    } else {
                        var responseData = new String(delivery.getBody());
                        LOGGER.info(() -> String.format("Message '%s' Received response: %s", message.getMessageId(), responseData));

                        switch (RabbitMqReplyType.valueOf(repliedProperties.getType().toUpperCase())) {
                            case EXCEPTION -> {
                                var exception = serializer.deserialize(responseData, RuntimeException.class);
                                future.completeExceptionally(exception);
                            }
                            case EMPTY -> future.complete(null);
                            case MESSAGE -> future.complete(serializer.deserialize(responseData, responseType));
                            default ->
                                future.completeExceptionally(new MessageTransportException("Unknown response type: " + repliedProperties.getType()));
                        }
                    }
                } finally {
                    // 一旦收到匹配的响应，取消临时的回复消费者
                    cancelConsumer(channel, consumerTag);
                }
            }, consumerTag -> {
                // 如果消费者在 future 完成之前被意外取消，
                // 发出错误信号，避免调用者无限等待
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new MessageDeliverException("Consumer canceled before receiving response for message: " + message.getMessageId()));
                }
            });

            // 添加超时以防止调用者无限等待。
            // 当 future 完成（成功、失败或超时）时关闭回复通道。
            var timeout = getResponseTimeout();
            future.orTimeout(timeout, TimeUnit.MILLISECONDS)
                  .whenComplete((r, ex) -> {
                      if (ex instanceof TimeoutException) {
                          LOGGER.warning(() -> String.format("Message '%s' timed out waiting for response", message.getMessageId()));
                      }
                      closeChannel(channel);
                  });

            // 发布命令消息（带重试），并在失败时取消消费者
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

    /**
     * 以 RPC 模式调用消息，发送请求到指定队列并等待响应。
     * <p>
     * 与 {@link #sendAsync(RoutedMessage, Class)} 相比，此方法额外设置了 AMQP
     * {@code replyTo} 属性，使服务端可以将响应发送回调用者声明的临时队列。
     *
     * @param <M>          消息类型
     * @param <R>          响应类型
     * @param message      要发送的路由消息
     * @param responseType 期望的响应类型
     * @return 包含响应的异步结果
     */
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

            // 在发布前注册响应消费者，确保准备好接收回复
            String replyTag = channel.basicConsume(responseQueueName, true, (consumerTag, delivery) -> {

                var repliedProperties = delivery.getProperties();

                if (!Objects.equals(repliedProperties.getCorrelationId(), message.getCorrelationId())) {
                    return;
                }

                var responseData = new String(delivery.getBody());
                LOGGER.info(() -> String.format("Received response for message '%s': %s", message.getMessageId(), responseData));

                try {
                    switch (RabbitMqReplyType.valueOf(repliedProperties.getType().toUpperCase())) {
                        case EXCEPTION -> {
                            var exception = serializer.deserialize(responseData, RuntimeException.class);
                            future.completeExceptionally(exception);
                        }
                        case EMPTY -> future.complete(null);
                        case MESSAGE -> future.complete(serializer.deserialize(responseData, responseType));
                        default ->
                            future.completeExceptionally(new MessageTransportException("Unknown response type: " + repliedProperties.getType()));
                    }
                } finally {
                    // 一旦收到匹配的响应，取消临时的回复消费者
                    cancelConsumer(channel, consumerTag);
                }
            }, consumerTag -> {
                // 如果消费者在 future 完成之前被意外取消，
                // 发出错误信号，避免调用者无限等待
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new MessageDeliverException("Consumer canceled before receiving response for message: " + message.getMessageId()));
                }
            });

            // 添加超时以防止调用者无限等待。
            // 当 future 完成（成功、失败或超时）时关闭回复通道。
            var timeout = getResponseTimeout();
            future.orTimeout(timeout, TimeUnit.MILLISECONDS)
                  .whenComplete((r, ex) -> {
                      if (ex instanceof TimeoutException) {
                          LOGGER.warning(() -> String.format("Message '%s' timed out waiting for response", message.getMessageId()));
                      }
                      closeChannel(channel);
                  });

            // 发布请求消息（带重试），并在失败时取消消费者
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
     * 基于重试策略参数计算响应超时时间。
     * <p>
     * 超时时间计算方式为 {@code retryDelay * (retryAttempts + 1)} 加上
     * 网络往返和处理器处理时间的缓冲。
     */
    private long getResponseTimeout() {
        // 基础值：总重试窗口 + 5 秒缓冲，用于处理器处理和网络延迟
        return options.getRetryDelay() * (options.getRetryAttempts() + 1) + 5_000L;
    }

    /**
     * 安全地关闭通道，记录任何错误但不传播异常。
     *
     * @param channel 要关闭的通道
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
     * 安全地取消消费者，记录任何错误但不传播异常。
     *
     * @param channel     消费者所属的通道
     * @param consumerTag 要取消的消费者标签
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
     * @param channel         用于检查队列的通道
     * @param queueNamePrefix 要检查的队列名称前缀
     * @param channelName     与队列关联的通道名称
     * @return 完整的队列名称（如果存在且有消费者）
     * @throws MessageDeliverException 如果队列不存在或没有消费者
     */
    private String checkQueue(Channel channel, String queueNamePrefix, String channelName) {
        var requestQueueName = options.generateQueueName(queueNamePrefix, channelName);

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

    /**
     * 创建基于 Failsafe 的重试策略。
     * <p>
     * 处理 IOException 和 TimeoutException，使用配置的延迟和最大重试次数。
     *
     * @return 配置好的重试策略
     */
    private RetryPolicy<Object> createRetryPolicy() {
        return RetryPolicy.builder()
                          .handle(IOException.class)
                          .handle(TimeoutException.class)
                          .withDelay(Duration.ofMillis(options.getRetryDelay()))
                          .withMaxRetries(options.getRetryAttempts())
                          .build();
    }
}
