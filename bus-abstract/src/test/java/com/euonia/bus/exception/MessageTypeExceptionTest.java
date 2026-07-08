package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageTypeException}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageTypeException")
class MessageTypeExceptionTest {

    @Test
    @DisplayName("should create with message and be a RuntimeException")
    void shouldCreateWithMessage() {
        var ex = new MessageTypeException("invalid message type");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("invalid message type");
    }
}
