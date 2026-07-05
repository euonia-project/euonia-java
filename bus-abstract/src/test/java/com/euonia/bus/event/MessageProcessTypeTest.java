package com.euonia.bus.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageProcessType} 枚举。
 */
@SuppressWarnings("unused")
@DisplayName("MessageProcessType")
class MessageProcessTypeTest {

    @Test
    @DisplayName("should contain all six lifecycle stages")
    void shouldContainAllSixValues() {
        assertThat(MessageProcessType.values())
            .containsExactly(
                MessageProcessType.SEND,
                MessageProcessType.DELIVERED,
                MessageProcessType.RECEIVED,
                MessageProcessType.ACKNOWLEDGED,
                MessageProcessType.REPLIED,
                MessageProcessType.HANDLED
            );
    }

    @Test
    @DisplayName("should resolve enum by name")
    void shouldResolveByName() {
        assertThat(MessageProcessType.valueOf("SEND")).isEqualTo(MessageProcessType.SEND);
        assertThat(MessageProcessType.valueOf("RECEIVED")).isEqualTo(MessageProcessType.RECEIVED);
    }
}
