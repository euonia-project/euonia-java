package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageConventionException}。
 */
@SuppressWarnings("unused")
@DisplayName("MessageConventionException")
class MessageConventionExceptionTest {

    @Test
    @DisplayName("should create with message and be a RuntimeException")
    void shouldCreateWithMessage() {
        var ex = new MessageConventionException("convention violation");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("convention violation");
    }
}
