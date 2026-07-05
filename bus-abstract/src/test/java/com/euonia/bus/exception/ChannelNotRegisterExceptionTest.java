package com.euonia.bus.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link ChannelNotRegisterException}。
 */
@SuppressWarnings("unused")
@DisplayName("ChannelNotRegisterException")
class ChannelNotRegisterExceptionTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create with channel and format default message")
        void shouldCreateWithChannel() {
            var ex = new ChannelNotRegisterException("orders");

            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex.getChannel()).isEqualTo("orders");
            assertThat(ex.getMessage()).contains("'orders'").contains("not registered");
        }

        @Test
        @DisplayName("should create with channel and custom message")
        void shouldCreateWithChannelAndCustomMessage() {
            var ex = new ChannelNotRegisterException("payments", "custom error detail");

            assertThat(ex.getChannel()).isEqualTo("payments");
            assertThat(ex.getMessage()).isEqualTo("custom error detail");
        }
    }
}
