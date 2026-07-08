package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageTransportException}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageTransportException")
class MessageTransportExceptionTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create with message only")
        void shouldCreateWithMessage() {
            var ex = new MessageTransportException("network error");

            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex.getMessage()).isEqualTo("network error");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            var cause = new RuntimeException("root cause");
            var ex = new MessageTransportException("transport failed", cause);

            assertThat(ex.getMessage()).isEqualTo("transport failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}
