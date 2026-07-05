package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link RabbitMqReplyType} 枚举。
 */
@SuppressWarnings("unused")
@DisplayName("RabbitMqReplyType")
class RabbitMqReplyTypeTest {

    @Nested
    @DisplayName("values")
    class Values {

        @Test
        @DisplayName("should contain three reply types")
        void shouldContainThreeValues() {
            assertThat(RabbitMqReplyType.values())
                .containsExactly(
                    RabbitMqReplyType.EXCEPTION,
                    RabbitMqReplyType.EMPTY,
                    RabbitMqReplyType.MESSAGE
                );
        }

        @Test
        @DisplayName("should resolve by name")
        void shouldResolveByName() {
            assertThat(RabbitMqReplyType.valueOf("EXCEPTION")).isEqualTo(RabbitMqReplyType.EXCEPTION);
            assertThat(RabbitMqReplyType.valueOf("EMPTY")).isEqualTo(RabbitMqReplyType.EMPTY);
            assertThat(RabbitMqReplyType.valueOf("MESSAGE")).isEqualTo(RabbitMqReplyType.MESSAGE);
        }

        @Test
        @DisplayName("should return correct string value")
        void shouldReturnCorrectStringValue() {
            assertThat(RabbitMqReplyType.EXCEPTION.getValue()).isEqualTo("EXCEPTION");
            assertThat(RabbitMqReplyType.EMPTY.getValue()).isEqualTo("EMPTY");
            assertThat(RabbitMqReplyType.MESSAGE.getValue()).isEqualTo("MESSAGE");
        }
    }
}
