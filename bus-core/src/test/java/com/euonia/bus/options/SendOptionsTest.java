package com.euonia.bus.options;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link SendOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("SendOptions")
class SendOptionsTest {

    @Nested
    @DisplayName("correlation id")
    class CorrelationId {

        @Test
        @DisplayName("should be null by default")
        void shouldBeNullByDefault() {
            var opts = new SendOptions();

            assertThat(opts.getCorrelationId()).isNull();
        }

        @Test
        @DisplayName("should set and get correlation id")
        void shouldSetCorrelationId() {
            var opts = new SendOptions();
            opts.setCorrelationId("corr-123");

            assertThat(opts.getCorrelationId()).isEqualTo("corr-123");
        }
    }

    @Nested
    @DisplayName("inherited from ExtendableOptions")
    class InheritedFields {

        @Test
        @DisplayName("should auto-generate message id")
        void shouldAutoGenerateMessageId() {
            var opts = new SendOptions();

            assertThat(opts.getMessageId()).isNotNull();
        }

        @Test
        @DisplayName("should set channel and queue")
        void shouldSetChannelAndQueue() {
            var opts = new SendOptions();
            opts.setChannel("commands");
            opts.setQueue("command-queue");

            assertThat(opts.getChannel()).isEqualTo("commands");
            assertThat(opts.getQueue()).isEqualTo("command-queue");
        }

        @Test
        @DisplayName("should set priority and default to 0")
        void shouldSetPriority() {
            var opts = new SendOptions();

            assertThat(opts.getPriority()).isZero();
            opts.setPriority(3);
            assertThat(opts.getPriority()).isEqualTo(3);
        }
    }
}
