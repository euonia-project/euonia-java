package com.euonia.bus.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageProcessedEvent}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageProcessedEvent")
class MessageProcessedEventTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should store message, context, and process type")
        void shouldStoreAllFields() {
            var event = new MessageProcessedEvent("msg", null, MessageProcessType.SEND);

            assertThat(event.getMessage()).isEqualTo("msg");
            assertThat(event.getContext()).isNull();
            assertThat(event.getProcessType()).isEqualTo(MessageProcessType.SEND);
        }

        @Test
        @DisplayName("should support all process types")
        void shouldSupportAllProcessTypes() {
            for (MessageProcessType type : MessageProcessType.values()) {
                var event = new MessageProcessedEvent("msg", null, type);
                assertThat(event.getProcessType()).isEqualTo(type);
            }
        }
    }
}
