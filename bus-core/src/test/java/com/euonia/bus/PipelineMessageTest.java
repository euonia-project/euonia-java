package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link PipelineMessage} 的消息封装。
 */
@SuppressWarnings("unused")
@DisplayName("PipelineMessage")
class PipelineMessageTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should store message")
        void shouldStoreMessage() {
            var pm = new PipelineMessage<>("hello");

            assertThat(pm.getMessage()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should allow null message")
        void shouldAllowNullMessage() {
            var pm = new PipelineMessage<>(null);

            assertThat(pm.getMessage()).isNull();
        }

        @Test
        @DisplayName("should have null pipeline initially")
        void shouldHaveNullPipelineInitially() {
            var pm = new PipelineMessage<>("msg");

            assertThat(pm.getPipeline()).isNull();
        }
    }
}
