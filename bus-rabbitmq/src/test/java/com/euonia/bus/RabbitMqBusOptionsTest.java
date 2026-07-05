package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link RabbitMqBusOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("RabbitMqBusOptions")
class RabbitMqBusOptionsTest {

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("should be enabled by default")
        void shouldBeEnabledByDefault() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should have default name from class simple name")
        void shouldHaveDefaultName() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getName()).isEqualTo("RabbitMqTransport");
        }

        @Test
        @DisplayName("should have default host localhost")
        void shouldHaveDefaultHost() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getHost()).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should have default port 5672")
        void shouldHaveDefaultPort() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getPort()).isEqualTo(5672);
        }

        @Test
        @DisplayName("should have default username guest")
        void shouldHaveDefaultUsername() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getUsername()).isEqualTo("guest");
        }

        @Test
        @DisplayName("should have default password guest")
        void shouldHaveDefaultPassword() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getPassword()).isEqualTo("guest");
        }

        @Test
        @DisplayName("should have default virtual host /")
        void shouldHaveDefaultVirtualHost() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getVirtualHost()).isEqualTo("/");
        }

        @Test
        @DisplayName("should be mandatory by default")
        void shouldBeMandatoryByDefault() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.isMandatory()).isTrue();
        }

        @Test
        @DisplayName("should have default prefetch count of 1")
        void shouldHaveDefaultPrefetchCount() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getPrefetchCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should have default retry delay of 5000ms")
        void shouldHaveDefaultRetryDelay() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getRetryDelay()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("should have default retry attempts of 3")
        void shouldHaveDefaultRetryAttempts() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getRetryAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("should have dead letter enabled by default")
        void shouldHaveDeadLetterEnabledByDefault() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.isDeadLetterEnabled()).isTrue();
        }

        @Test
        @DisplayName("should have default exchange name prefix")
        void shouldHaveDefaultExchangeNamePrefix() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getExchangeNamePrefix()).isEqualTo("$nerosoft.euonia.exchange");
        }

        @Test
        @DisplayName("should have default queue name prefix")
        void shouldHaveDefaultQueueNamePrefix() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getQueueNamePrefix()).isEqualTo("$nerosoft.euonia.queue");
        }

        @Test
        @DisplayName("should have default rpc queue prefix")
        void shouldHaveDefaultRpcQueuePrefix() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getRpcQueuePrefix()).isEqualTo("$nerosoft.euonia.request");
        }

        @Test
        @DisplayName("should have default dead letter exchange prefix")
        void shouldHaveDefaultDeadLetterExchangePrefix() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getDeadLetterExchangePrefix()).isEqualTo("$nerosoft.euonia.dlx");
        }

        @Test
        @DisplayName("should have default dead letter queue prefix")
        void shouldHaveDefaultDeadLetterQueuePrefix() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getDeadLetterQueuePrefix()).isEqualTo("$nerosoft.euonia.dlq");
        }

        @Test
        @DisplayName("should have null subscription id by default")
        void shouldHaveNullSubscriptionId() {
            var opts = new RabbitMqBusOptions();

            assertThat(opts.getSubscriptionId()).isNull();
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("should set enabled via supplier")
        void shouldSetEnabled() {
            var opts = new RabbitMqBusOptions();
            opts.setEnabled(() -> false);

            assertThat(opts.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("should set name")
        void shouldSetName() {
            var opts = new RabbitMqBusOptions();
            opts.setName("MyRabbitMq");

            assertThat(opts.getName()).isEqualTo("MyRabbitMq");
        }

        @Test
        @DisplayName("should set host and port")
        void shouldSetHostAndPort() {
            var opts = new RabbitMqBusOptions();
            opts.setHost("rabbitmq.local");
            opts.setPort(5673);

            assertThat(opts.getHost()).isEqualTo("rabbitmq.local");
            assertThat(opts.getPort()).isEqualTo(5673);
        }

        @Test
        @DisplayName("should set credentials")
        void shouldSetCredentials() {
            var opts = new RabbitMqBusOptions();
            opts.setUsername("admin");
            opts.setPassword("secret");

            assertThat(opts.getUsername()).isEqualTo("admin");
            assertThat(opts.getPassword()).isEqualTo("secret");
        }

        @Test
        @DisplayName("should set virtual host")
        void shouldSetVirtualHost() {
            var opts = new RabbitMqBusOptions();
            opts.setVirtualHost("/vhost");

            assertThat(opts.getVirtualHost()).isEqualTo("/vhost");
        }

        @Test
        @DisplayName("should set mandatory flag")
        void shouldSetMandatory() {
            var opts = new RabbitMqBusOptions();
            opts.setMandatory(false);

            assertThat(opts.isMandatory()).isFalse();
        }

        @Test
        @DisplayName("should set prefetch count")
        void shouldSetPrefetchCount() {
            var opts = new RabbitMqBusOptions();
            opts.setPrefetchCount(10);

            assertThat(opts.getPrefetchCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("should set retry config")
        void shouldSetRetryConfig() {
            var opts = new RabbitMqBusOptions();
            opts.setRetryDelay(10000L);
            opts.setRetryAttempts(5);

            assertThat(opts.getRetryDelay()).isEqualTo(10000L);
            assertThat(opts.getRetryAttempts()).isEqualTo(5);
        }

        @Test
        @DisplayName("should set dead letter config")
        void shouldSetDeadLetterConfig() {
            var opts = new RabbitMqBusOptions();
            opts.setDeadLetterEnabled(false);
            opts.setDeadLetterExchangePrefix("custom.dlx");
            opts.setDeadLetterQueuePrefix("custom.dlq");

            assertThat(opts.isDeadLetterEnabled()).isFalse();
            assertThat(opts.getDeadLetterExchangePrefix()).isEqualTo("custom.dlx");
            assertThat(opts.getDeadLetterQueuePrefix()).isEqualTo("custom.dlq");
        }

        @Test
        @DisplayName("should set subscription id")
        void shouldSetSubscriptionId() {
            var opts = new RabbitMqBusOptions();
            opts.setSubscriptionId("sub-rabbit-1");

            assertThat(opts.getSubscriptionId()).isEqualTo("sub-rabbit-1");
        }

        @Test
        @DisplayName("should set connection")
        void shouldSetConnection() {
            var opts = new RabbitMqBusOptions();
            opts.setConnection("amqp://custom:5672");

            assertThat(opts.getConnection()).isEqualTo("amqp://custom:5672");
        }
    }

    @Nested
    @DisplayName("connection string")
    class ConnectionString {

        @Test
        @DisplayName("should auto-build connection string from host and port")
        void shouldAutoBuildConnectionString() {
            var opts = new RabbitMqBusOptions();

            var conn = opts.getConnection();

            assertThat(conn).isEqualTo("amqp://guest:guest@localhost:5672//");
        }

        @Test
        @DisplayName("should return explicit connection string when set")
        void shouldReturnExplicitConnectionWhenSet() {
            var opts = new RabbitMqBusOptions();
            opts.setConnection("amqp://custom:5672/myvh");

            assertThat(opts.getConnection()).isEqualTo("amqp://custom:5672/myvh");
        }
    }
}
