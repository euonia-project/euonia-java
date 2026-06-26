package com.euonia.bus;

import java.util.Map;

import com.euonia.utility.Assert;

/**
 * Kafka 总线传输的配置选项。
 * <p>
 * 封装 Kafka 生产者/消费者配置以及 Euonia 特定的消息处理参数。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class KafkaBusOptions {

    private String name = KafkaTransport.class.getSimpleName();
    private String bootstrapServers = "localhost:9092";
    private String topicPrefix = KafkaConstants.DEFAULT_TOPIC_PREFIX;
    private String groupId = System.getProperty("euonia.kafka.groupId", "euonia-consumer");
    private int concurrency = 1;
    private long retryDelay = 5000L;
    private int retryAttempts = 3;
    private Map<String, Object> producerConfig = Map.of(
        "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer"
    );
    private Map<String, Object> consumerConfig = Map.of(
        "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
        "value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer",
        "enable.auto.commit", false
    );
    private String subscriptionId;

    // ──── getters / setters ────

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Map<String, Object> getProducerConfig() {
        return producerConfig;
    }

    public void setProducerConfig(Map<String, Object> producerConfig) {
        this.producerConfig = producerConfig;
    }

    public Map<String, Object> getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(Map<String, Object> consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * 生成 Kafka 主题名称。
     */
    String generateTopicName(String channelName) {
        Assert.notNull(subscriptionId, "subscriptionId cannot be null");
        return String.format("%s:%s@%s", topicPrefix, channelName, subscriptionId);
    }
}
