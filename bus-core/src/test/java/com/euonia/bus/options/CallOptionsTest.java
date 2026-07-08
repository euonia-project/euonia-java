package com.euonia.bus.options;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link CallOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("CallOptions")
class CallOptionsTest {

    @Nested
    @DisplayName("correlation id")
    class CorrelationId {

        @Test
        @DisplayName("should be null by default")
        void shouldBeNullByDefault() {
            var opts = new CallOptions();

            assertThat(opts.getCorrelationId()).isNull();
        }

        @Test
        @DisplayName("should set and get correlation id")
        void shouldSetCorrelationId() {
            var opts = new CallOptions();
            opts.setCorrelationId("call-corr-456");

            assertThat(opts.getCorrelationId()).isEqualTo("call-corr-456");
        }
    }

    @Nested
    @DisplayName("inherited from ExtendableOptions")
    class InheritedFields {

        @Test
        @DisplayName("should auto-generate message id")
        void shouldAutoGenerateMessageId() {
            var opts = new CallOptions();

            assertThat(opts.getMessageId()).isNotNull();
        }

        @Test
        @DisplayName("should set timeout and delay")
        void shouldSetTimeoutAndDelay() {
            var opts = new CallOptions();
            opts.setTimeout(5000);
            opts.setDelay(200);

            assertThat(opts.getTimeout()).isEqualTo(5000);
            assertThat(opts.getDelay()).isEqualTo(200);
        }

        @Test
        @DisplayName("should set custom message id")
        void shouldSetCustomMessageId() {
            var opts = new CallOptions();
            opts.setMessageId("custom-id");

            assertThat(opts.getMessageId()).isEqualTo("custom-id");
        }

        @Test
        @DisplayName("should disable default pipeline behaviors")
        void shouldDisableDefaultPipelineBehaviors() {
            var opts = new CallOptions();
            opts.setAttachDefaultPipelineBehaviors(false);

            assertThat(opts.isAttachDefaultPipelineBehaviors()).isFalse();
        }
    }
}
