package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link KafkaBusOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("KafkaBusOptions")
class KafkaBusOptionsTest {

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("should have default name from class simple name")
        void shouldHaveDefaultName() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getName()).isEqualTo("KafkaTransport");
        }

        @Test
        @DisplayName("should have default bootstrap servers")
        void shouldHaveDefaultBootstrapServers() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getBootstrapServers()).isEqualTo("localhost:9092");
        }

        @Test
        @DisplayName("should have default topic prefix")
        void shouldHaveDefaultTopicPrefix() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getTopicPrefix()).isEqualTo("$nerosoft.euonia.topic");
        }

        @Test
        @DisplayName("should have default group id")
        void shouldHaveDefaultGroupId() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getGroupId()).isEqualTo("euonia-consumer");
        }

        @Test
        @DisplayName("should have default concurrency of 1")
        void shouldHaveDefaultConcurrency() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getConcurrency()).isEqualTo(1);
        }

        @Test
        @DisplayName("should have default retry delay of 5000ms")
        void shouldHaveDefaultRetryDelay() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getRetryDelay()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("should have default retry attempts of 3")
        void shouldHaveDefaultRetryAttempts() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getRetryAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("should have default producer config")
        void shouldHaveDefaultProducerConfig() {
            var opts = new KafkaBusOptions();

            var config = opts.getProducerConfig();
            assertThat(config).containsKeys("key.serializer", "value.serializer");
        }

        @Test
        @DisplayName("should have default consumer config with auto commit disabled")
        void shouldHaveDefaultConsumerConfig() {
            var opts = new KafkaBusOptions();

            var config = opts.getConsumerConfig();
            assertThat(config).containsKeys("key.deserializer", "value.deserializer", "enable.auto.commit");
            assertThat(config.get("enable.auto.commit")).isEqualTo(false);
        }

        @Test
        @DisplayName("should have null subscription id by default")
        void shouldHaveNullSubscriptionId() {
            var opts = new KafkaBusOptions();

            assertThat(opts.getSubscriptionId()).isNull();
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("should set name")
        void shouldSetName() {
            var opts = new KafkaBusOptions();
            opts.setName("MyKafka");

            assertThat(opts.getName()).isEqualTo("MyKafka");
        }

        @Test
        @DisplayName("should set bootstrap servers")
        void shouldSetBootstrapServers() {
            var opts = new KafkaBusOptions();
            opts.setBootstrapServers("kafka:9092");

            assertThat(opts.getBootstrapServers()).isEqualTo("kafka:9092");
        }

        @Test
        @DisplayName("should set topic prefix")
        void shouldSetTopicPrefix() {
            var opts = new KafkaBusOptions();
            opts.setTopicPrefix("custom.prefix");

            assertThat(opts.getTopicPrefix()).isEqualTo("custom.prefix");
        }

        @Test
        @DisplayName("should set group id")
        void shouldSetGroupId() {
            var opts = new KafkaBusOptions();
            opts.setGroupId("my-group");

            assertThat(opts.getGroupId()).isEqualTo("my-group");
        }

        @Test
        @DisplayName("should set concurrency")
        void shouldSetConcurrency() {
            var opts = new KafkaBusOptions();
            opts.setConcurrency(4);

            assertThat(opts.getConcurrency()).isEqualTo(4);
        }

        @Test
        @DisplayName("should set retry delay")
        void shouldSetRetryDelay() {
            var opts = new KafkaBusOptions();
            opts.setRetryDelay(10000L);

            assertThat(opts.getRetryDelay()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("should set retry attempts")
        void shouldSetRetryAttempts() {
            var opts = new KafkaBusOptions();
            opts.setRetryAttempts(5);

            assertThat(opts.getRetryAttempts()).isEqualTo(5);
        }

        @Test
        @DisplayName("should set producer config")
        void shouldSetProducerConfig() {
            var opts = new KafkaBusOptions();
            opts.setProducerConfig(java.util.Map.of("custom", "value"));

            assertThat(opts.getProducerConfig()).containsEntry("custom", "value");
        }

        @Test
        @DisplayName("should set consumer config")
        void shouldSetConsumerConfig() {
            var opts = new KafkaBusOptions();
            opts.setConsumerConfig(java.util.Map.of("custom.consumer", "val"));

            assertThat(opts.getConsumerConfig()).containsEntry("custom.consumer", "val");
        }

        @Test
        @DisplayName("should set subscription id")
        void shouldSetSubscriptionId() {
            var opts = new KafkaBusOptions();
            opts.setSubscriptionId("sub-123");

            assertThat(opts.getSubscriptionId()).isEqualTo("sub-123");
        }
    }

    @Nested
    @DisplayName("config details")
    class ConfigDetails {

        @Test
        @DisplayName("producer config should contain byte array serializer")
        void producerConfigShouldContainByteArraySerializer() {
            var opts = new KafkaBusOptions();

            var config = opts.getProducerConfig();
            assertThat(config.get("value.serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.ByteArraySerializer");
        }

        @Test
        @DisplayName("consumer config should have auto commit disabled")
        void consumerConfigShouldHaveAutoCommitDisabled() {
            var opts = new KafkaBusOptions();

            var config = opts.getConsumerConfig();
            assertThat(config.get("enable.auto.commit")).isEqualTo(false);
        }

        @Test
        @DisplayName("should allow custom config override")
        void shouldAllowCustomConfigOverride() {
            var opts = new KafkaBusOptions();
            opts.setProducerConfig(java.util.Map.of(
                "bootstrap.servers", "custom:9092",
                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer"
            ));

            assertThat(opts.getProducerConfig()).containsEntry("bootstrap.servers", "custom:9092");
        }
    }
}
