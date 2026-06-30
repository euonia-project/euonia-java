package com.euonia.bus;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.euonia.bus.serialization.MessageSerializer;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

/**
 * Kafka 传输实现，负责通过 Kafka 代理发布、发送和调用消息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class KafkaTransport implements Transport, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(KafkaTransport.class.getName());

    private final KafkaProducer<String, byte[]> producer;
    private final MessageSerializer serializer;
    private final KafkaBusOptions options;
    private final RetryPolicy<Object> retryPolicy;

    public KafkaTransport(KafkaBusOptions options, MessageSerializer serializer) {
        this.serializer = serializer;
        this.options = options;
        this.producer = createProducer();
        this.retryPolicy = createRetryPolicy();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public <M> CompletableFuture<Void> publishAsync(RoutedMessage<M> message) {
        var topicName = options.generateTopicName(message.getChannel());
        var data = serializer.serializeToBytes(message);

        return Failsafe.with(retryPolicy).runAsync(() -> {
            var record = new ProducerRecord<>(topicName, message.getMessageId(), data);
            producer.send(record);
            LOGGER.info(() -> String.format("Message '%s' published to topic %s", message.getMessageId(), topicName));
        });
    }

    @Override
    public <M> CompletableFuture<Void> sendAsync(RoutedMessage<M> message) {
        return publishAsync(message);
    }

    @Override
    public <M, R> CompletableFuture<R> sendAsync(RoutedMessage<M> message, Class<R> responseType) {
        return callAsync(message, responseType);
    }

    @Override
    public <M, R> CompletableFuture<R> callAsync(RoutedMessage<M> message, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();
        var topicName = options.generateTopicName(message.getChannel());
        var replyTopic = topicName + ".reply";

        var data = serializer.serializeToBytes(message);

        new Thread(() -> {
            try (var replyConsumer = createReplyConsumer()) {
                replyConsumer.subscribe(Collections.singletonList(replyTopic));
                while (!future.isDone()) {
                    var records = replyConsumer.poll(Duration.ofSeconds(2));
                    for (var record : records) {
                        var responseData = new String(record.value());
                        try {
                            future.complete(serializer.deserialize(responseData, responseType));
                            return;
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                            return;
                        }
                    }
                }
            }
        }).start();

        Failsafe.with(retryPolicy).runAsync(() -> {
            var record = new ProducerRecord<>(topicName, message.getMessageId(), data);
            record.headers().add(new RecordHeader("replyTo", replyTopic.getBytes()));
            producer.send(record);
            LOGGER.info(() -> String.format("Message '%s' call sent to topic %s", message.getMessageId(), topicName));
        }).exceptionally(ex -> {
            LOGGER.severe(() -> String.format("Message '%s' call failed after retries: %s", message.getMessageId(), ex.getMessage()));
            if (!future.isDone()) {
                future.completeExceptionally(ex);
            }
            return null;
        });

        return future;
    }

    private KafkaConsumer<String, byte[]> createReplyConsumer() {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, options.getGroupId() + "-reply");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    private KafkaProducer<String, byte[]> createProducer() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, options.getName() + "-producer");
        props.putAll(options.getProducerConfig());
        return new KafkaProducer<>(props);
    }

    @Override
    public void close() {
        producer.close();
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
