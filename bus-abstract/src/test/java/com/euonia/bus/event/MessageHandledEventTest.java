package com.euonia.bus.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageHandledEvent}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageHandledEvent")
class MessageHandledEventTest {

    @Nested
    @DisplayName("construction and fields")
    class ConstructionAndFields {

        @Test
        @DisplayName("should have HANDLED process type")
        void shouldHaveHandledProcessType() {
            var event = new MessageHandledEvent("msg");

            assertThat(event.getProcessType()).isEqualTo(MessageProcessType.HANDLED);
            assertThat(event.getMessage()).isEqualTo("msg");
        }

        @Test
        @DisplayName("should default handler type to null")
        void shouldDefaultHandlerTypeToNull() {
            var event = new MessageHandledEvent("msg");

            assertThat(event.getHandlerType()).isNull();
        }

        @Test
        @DisplayName("should set and get handler type")
        void shouldSetHandlerType() {
            var event = new MessageHandledEvent("msg");
            event.setHandlerType(String.class);

            assertThat(event.getHandlerType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should extend MessageProcessedEvent")
        void shouldExtendMessageProcessedEvent() {
            var event = new MessageHandledEvent("msg");

            assertThat(event).isInstanceOf(MessageProcessedEvent.class);
        }
    }
}
