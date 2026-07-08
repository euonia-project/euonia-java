package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageDeliverException}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageDeliverException")
class MessageDeliverExceptionTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create with default message when no args")
        void shouldCreateWithDefaultMessage() {
            var ex = new MessageDeliverException();

            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex.getMessage()).isEqualTo("Error occurred during message deliver.");
        }

        @Test
        @DisplayName("should create with custom message")
        void shouldCreateWithCustomMessage() {
            var ex = new MessageDeliverException("custom delivery error");

            assertThat(ex.getMessage()).isEqualTo("custom delivery error");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            var cause = new RuntimeException("connection lost");
            var ex = new MessageDeliverException("delivery failed", cause);

            assertThat(ex.getMessage()).isEqualTo("delivery failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}
