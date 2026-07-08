package com.euonia.bus;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Consumer;
import com.euonia.bus.serialization.MessageSerializer;

/**
 * Kafka 队列消费者，实现单播消息的消费。
 * <p>
 * 从 Kafka 主题消费消息，处理后通过回复主题发送响应。
 *
 * @author damon(zhaorong@outlook.com)
 */
final class KafkaQueueConsumer extends KafkaRecipient implements Consumer {

    /** Kafka 消费者 */
    private KafkaConsumer<String, byte[]> consumer;
    /** 用于发送回复的 Kafka 生产者 */
    private KafkaProducer<String, byte[]> replyProducer;

    /**
     * 使用必要的依赖构造队列消费者。
     *
     * @param options     Kafka 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 消息类型
     */
    public KafkaQueueConsumer(KafkaBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
        super(options, handler, serializer, messageType);
    }

    /**
     * 启动队列消费，在独立线程中轮询并处理消息，并发送回复。
     *
     * @param channelName 通道名称
     */
    @Override
    void start(String channelName) {
        var topicName = options.generateTopicName(channelName);
        consumer = createConsumer(channelName);
        replyProducer = createReplyProducer();
        consumer.subscribe(java.util.Collections.singletonList(topicName));

        new Thread(() -> {
            while (true) {
                var records = consumer.poll(Duration.ofSeconds(1));
                for (var record : records) {
                    var body = new String(record.value());
                    MessageEnvelope<?> message = serializer.deserializeEnvelope(body, messageType);
                    var context = new MessageContextBase(message);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handler.handleAsync(channelName, getName(), message, context)
                           .whenComplete((result, error) -> {
                               consumer.commitSync();
                               sendReply(replyProducer, record, message, result, error);
                               raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                           });
                }
            }
        });
    }

    private KafkaConsumer<String, byte[]> createConsumer(String channelName) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, options.getGroupId());
        props.putAll(options.getConsumerConfig());
        return new KafkaConsumer<>(props);
    }

    @Override
    public void close() {
        super.close();
        if (consumer != null) {
            consumer.close();
        }
        if (replyProducer != null) {
            replyProducer.close();
        }
    }

    /**
     * 创建用于发送回复的 Kafka 生产者。
     *
     * @return Kafka 生产者
     */
    private KafkaProducer<String, byte[]> createReplyProducer() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, options.getName() + "-reply-producer");
        return new KafkaProducer<>(props);
    }

    private void sendReply(KafkaProducer<String, byte[]> producer, org.apache.kafka.clients.consumer.ConsumerRecord<String, byte[]> request,
                           MessageEnvelope<?> message, Object result, Throwable error) {
        var replyTo = request.headers().lastHeader("replyTo");
        if (replyTo == null) {
            return;
        }
        var replyTopic = new String(replyTo.value());

        String type, data;
        if (error != null) {
            type = "exception";
            data = serializer.serialize(error);
        } else if (result != null) {
            type = "message";
            data = serializer.serialize(result);
        } else {
            type = "void";
            data = "";
        }

        var headers = new RecordHeaders();
        headers.add(new RecordHeader("type", type.getBytes()));
        var reply = new ProducerRecord<>(replyTopic, null, message.getMessageId(), data.getBytes(), headers);
        producer.send(reply);
    }
}
