package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link InMemoryBusOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("InMemoryBusOptions")
class InMemoryBusOptionsTest {

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("should have default transport name")
        void shouldHaveDefaultTransportName() {
            var opts = new InMemoryBusOptions();

            assertThat(opts.getName()).isEqualTo(InMemoryBusOptions.DEFAULT_TRANSPORT_NAME);
        }

        @Test
        @DisplayName("should have lazy initialize enabled by default")
        void shouldHaveLazyInitializeEnabled() {
            var opts = new InMemoryBusOptions();

            assertThat(opts.isLazyInitialize()).isTrue();
        }

        @Test
        @DisplayName("should have max concurrent calls of 1")
        void shouldHaveMaxConcurrentCallsOfOne() {
            var opts = new InMemoryBusOptions();

            assertThat(opts.getMaxConcurrentCalls()).isEqualTo(1);
        }

        @Test
        @DisplayName("should have multiple subscriber instance disabled by default")
        void shouldHaveMultipleSubscriberInstanceDisabled() {
            var opts = new InMemoryBusOptions();

            assertThat(opts.isMultipleSubscriberInstance()).isFalse();
        }

        @Test
        @DisplayName("should have dead letter enabled by default")
        void shouldHaveDeadLetterEnabledByDefault() {
            var opts = new InMemoryBusOptions();

            assertThat(opts.isDeadLetterEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("should set name")
        void shouldSetName() {
            var opts = new InMemoryBusOptions();
            opts.setName("CustomName");

            assertThat(opts.getName()).isEqualTo("CustomName");
        }

        @Test
        @DisplayName("should set lazy initialize")
        void shouldSetLazyInitialize() {
            var opts = new InMemoryBusOptions();
            opts.setLazyInitialize(false);

            assertThat(opts.isLazyInitialize()).isFalse();
        }

        @Test
        @DisplayName("should set max concurrent calls")
        void shouldSetMaxConcurrentCalls() {
            var opts = new InMemoryBusOptions();
            opts.setMaxConcurrentCalls(10);

            assertThat(opts.getMaxConcurrentCalls()).isEqualTo(10);
        }

        @Test
        @DisplayName("should set multiple subscriber instance")
        void shouldSetMultipleSubscriberInstance() {
            var opts = new InMemoryBusOptions();
            opts.setMultipleSubscriberInstance(true);

            assertThat(opts.isMultipleSubscriberInstance()).isTrue();
        }

        @Test
        @DisplayName("should set dead letter enabled")
        void shouldSetDeadLetterEnabled() {
            var opts = new InMemoryBusOptions();
            opts.setDeadLetterEnabled(false);

            assertThat(opts.isDeadLetterEnabled()).isFalse();
        }
    }
}
