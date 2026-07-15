package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageTransportNotFoundException}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageTransportNotFoundException")
class MessageTransportNotFoundExceptionTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create with transport name and format message")
        void shouldCreateWithTransportName() {
            var ex = new MessageTransportNotFoundException("kafka");

            assertThat(ex).isInstanceOf(MessageTransportException.class);
            assertThat(ex.getTransportName()).isEqualTo("kafka");
            assertThat(ex.getMessage()).contains("kafka").contains("not found");
        }

        @Test
        @DisplayName("should return correct name for different transports")
        void shouldReturnCorrectNameForDifferentTransports() {
            var ex = new MessageTransportNotFoundException("rabbitmq");

            assertThat(ex.getTransportName()).isEqualTo("rabbitmq");
            assertThat(ex.getMessage()).contains("rabbitmq");
        }
    }
}

