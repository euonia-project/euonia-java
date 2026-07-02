package com.euonia.bus;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Subscriber;
import com.euonia.bus.serialization.MessageSerializer;

/**
 * Kafka 主题订阅者，实现多播消息的消费。
 */
final class KafkaTopicSubscriber extends KafkaRecipient implements Subscriber {

    private KafkaConsumer<String, byte[]> consumer;

    public KafkaTopicSubscriber(KafkaBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
        super(options, handler, serializer, messageType);
    }

    @Override
    void start(String group) {
        var topicName = options.generateTopicName(group);
        consumer = createConsumer(group);
        consumer.subscribe(Collections.singletonList(topicName));

        new Thread(() -> {
            while (true) {
                var records = consumer.poll(Duration.ofSeconds(1));
                for (var record : records) {
                    var body = new String(record.value());
                    MessageEnvelope<?> message = serializer.deserializeEnvelope(body, messageType);
                    var context = new MessageContextBase(message);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handleAsync(message, context).whenComplete((result, error) -> {
                        consumer.commitSync();
                        raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                    });
                }
            }
        });
    }

    private KafkaConsumer<String, byte[]> createConsumer(String group) {
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
    }
}
