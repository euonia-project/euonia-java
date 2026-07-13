package com.euonia.bus.options;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link PublishOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("PublishOptions")
class PublishOptionsTest {

    @Nested
    @DisplayName("inherited from ExtendableOptions")
    class InheritedFields {

        @Test
        @DisplayName("should auto-generate message id")
        void shouldAutoGenerateMessageId() {
            var opts = new PublishOptions();

            assertThat(opts.getMessageId()).isNotNull();
        }

        @Test
        @DisplayName("should set and get channel")
        void shouldSetChannel() {
            var opts = new PublishOptions();
            opts.setChannel("orders");

            assertThat(opts.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should set and get priority")
        void shouldSetPriority() {
            var opts = new PublishOptions();
            opts.setPriority(5);

            assertThat(opts.getPriority()).isEqualTo(5);
        }

        @Test
        @DisplayName("should set and get timeout")
        void shouldSetTimeout() {
            var opts = new PublishOptions();
            opts.setTimeout(3000);

            assertThat(opts.getTimeout()).isEqualTo(3000);
        }

        @Test
        @DisplayName("should set and get delay")
        void shouldSetDelay() {
            var opts = new PublishOptions();
            opts.setDelay(1000);

            assertThat(opts.getDelay()).isEqualTo(1000);
        }

        @Test
        @DisplayName("should set metadata setter")
        void shouldSetMetadataSetter() {
            var opts = new PublishOptions();
            opts.setMetadataSetter(m -> m.put("key", "value"));

            assertThat(opts.getMetadataSetter()).isNotNull();
        }
    }
}
