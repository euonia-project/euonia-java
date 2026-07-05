package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 验证所有 {@link MessageHeaders} 常量值的一致性。
 */
@DisplayName("MessageHeaders")
class MessageHeadersTest {

    @Test
    @DisplayName("should define standard header constants")
    void shouldDefineStandardHeaderConstants() {
        assertThat(MessageHeaders.MESSAGE_ID).isEqualTo("x-message-id");
        assertThat(MessageHeaders.CORRELATION_ID).isEqualTo("x-correlation-id");
        assertThat(MessageHeaders.CONVERSATION_ID).isEqualTo("x-conversation-id");
        assertThat(MessageHeaders.CONTENT_TYPE).isEqualTo("x-content-type");
        assertThat(MessageHeaders.REQUEST_TRACE_ID).isEqualTo("x-request-trace-id");
        assertThat(MessageHeaders.AUTHORIZATION).isEqualTo("x-authorization");
    }
}
