package com.euonia.bus.dlq;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.MessageMetadata;

/**
 * 测试 {@link DeadLetterMessage}。
 */
@SuppressWarnings("unused")
@DisplayName("DeadLetterMessage")
class DeadLetterMessageTest {

    private static MessageEnvelope<String> createEnvelope(String messageId) {
        return new MessageEnvelope<>() {
            @Override public String getMessageId() { return messageId; }
            @Override public String getCorrelationId() { return null; }
            @Override public String getConversationId() { return null; }
            @Override public String getRequestTrackId() { return null; }
            @Override public String getChannel() { return "test"; }
            @Override public String getAuthorization() { return null; }
            @Override public long getTimestamp() { return System.currentTimeMillis(); }
            @Override public String getPayload() { return "payload"; }
            @Override public String getTypeName() { return String.class.getName(); }
            @Override public MessageMetadata getMetadata() { return new MessageMetadata(); }
        };
    }

    @Nested
    @DisplayName("construction with error")
    class ConstructionWithError {

        @Test
        @DisplayName("should store original message")
        void shouldStoreOriginalMessage() {
            var envelope = createEnvelope("msg-1");
            var dl = new DeadLetterMessage<>(envelope, new RuntimeException("fail"));

            assertThat(dl.getOriginalMessage()).isSameAs(envelope);
        }

        @Test
        @DisplayName("should extract reason from error message")
        void shouldExtractReasonFromErrorMessage() {
            var envelope = createEnvelope("msg-2");
            var dl = new DeadLetterMessage<>(envelope, new RuntimeException("connection timeout"));

            assertThat(dl.getReason()).isEqualTo("connection timeout");
        }

        @Test
        @DisplayName("should capture exception type name")
        void shouldCaptureExceptionTypeName() {
            var envelope = createEnvelope("msg-3");
            var dl = new DeadLetterMessage<>(envelope, new IllegalArgumentException("bad arg"));

            assertThat(dl.getExceptionType()).isEqualTo("java.lang.IllegalArgumentException");
        }

        @Test
        @DisplayName("should capture exception message detail")
        void shouldCaptureExceptionDetail() {
            var envelope = createEnvelope("msg-4");
            var error = new RuntimeException("detail");
            var dl = new DeadLetterMessage<>(envelope, error);

            assertThat(dl.getExceptionMessage()).isEqualTo(error.toString());
        }

        @Test
        @DisplayName("should set timestamp on creation")
        void shouldSetTimestampOnCreation() {
            var envelope = createEnvelope("msg-5");
            var before = System.currentTimeMillis();
            var dl = new DeadLetterMessage<>(envelope, new RuntimeException("err"));
            var after = System.currentTimeMillis();

            assertThat(dl.getTimestamp()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("construction with null error")
    class ConstructionWithNullError {

        @Test
        @DisplayName("should use unknown reason for null error")
        void shouldUseUnknownReasonForNullError() {
            var envelope = createEnvelope("msg-6");
            var dl = new DeadLetterMessage<>(envelope, null);

            assertThat(dl.getReason()).isEqualTo("Unknown");
            assertThat(dl.getExceptionType()).isNull();
            assertThat(dl.getExceptionMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include message id and reason")
        void shouldIncludeMessageIdAndReason() {
            var envelope = createEnvelope("msg-7");
            var dl = new DeadLetterMessage<>(envelope, new RuntimeException("test reason"));

            var str = dl.toString();

            assertThat(str).contains("test reason");
            assertThat(str).contains(envelope.getMessageId());
        }
    }
}
