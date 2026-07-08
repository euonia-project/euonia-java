package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageProcessingException}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageProcessingException")
class MessageProcessingExceptionTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create with no-arg constructor")
        void shouldCreateWithNoArgs() {
            var ex = new MessageProcessingException();

            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex.getMessage()).isNull();
        }

        @Test
        @DisplayName("should create with message only")
        void shouldCreateWithMessage() {
            var ex = new MessageProcessingException("processing failed");

            assertThat(ex.getMessage()).isEqualTo("processing failed");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            var cause = new IllegalArgumentException("bad arg");
            var ex = new MessageProcessingException("processing failed", cause);

            assertThat(ex.getMessage()).isEqualTo("processing failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}
