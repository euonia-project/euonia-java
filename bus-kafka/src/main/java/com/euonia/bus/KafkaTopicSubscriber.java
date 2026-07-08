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
 * <p>
 * 订阅 Kafka 主题，消费消息后触发确认并提交偏移量。
 *
 * @author damon(zhaorong@outlook.com)
 */
final class KafkaTopicSubscriber extends KafkaRecipient implements Subscriber {

    /** Kafka 消费者 */
    private KafkaConsumer<String, byte[]> consumer;

    /**
     * 使用必要的依赖构造主题订阅者。
     *
     * @param options     Kafka 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 消息类型
     */
    public KafkaTopicSubscriber(KafkaBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
        super(options, handler, serializer, messageType);
    }

    /**
     * 启动主题订阅，在独立线程中轮询并处理消息。
     *
     * @param channelName 通道名称
     */
    @Override
    void start(String channelName) {
        var topicName = options.generateTopicName(channelName);
        consumer = createConsumer(channelName);
        consumer.subscribe(Collections.singletonList(topicName));

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
                               raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                           });
                }
            }
        });
    }

    /**
     * 创建 Kafka 消费者。
     *
     * @param channelName 通道名称
     * @return 配置好的 Kafka 消费者
     */
    private KafkaConsumer<String, byte[]> createConsumer(String channelName) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, options.getGroupId());
        props.putAll(options.getConsumerConfig());
        return new KafkaConsumer<>(props);
    }

    /**
     * 关闭消费者。
     */
    @Override
    public void close() {
        super.close();
        if (consumer != null) {
            consumer.close();
        }
    }
}
