package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessagePack}。
 */
@SuppressWarnings("unused")
@DisplayName("MessagePack")
class MessagePackTest {

    private static MessageEnvelope<String> createEnvelope(String payload, String channel) {
        return new MessageEnvelope<>() {
            @Override public String getMessageId() { return "id-" + payload; }
            @Override public String getCorrelationId() { return null; }
            @Override public String getConversationId() { return null; }
            @Override public String getRequestTrackId() { return null; }
            @Override public String getChannel() { return channel; }
            @Override public String getAuthorization() { return null; }
            @Override public long getTimestamp() { return System.currentTimeMillis(); }
            @Override public String getPayload() { return payload; }
            @Override public String getTypeName() { return String.class.getName(); }
            @Override public MessageMetadata getMetadata() { return new MessageMetadata(); }
        };
    }

    @Nested
    @DisplayName("construction and accessors")
    class Construction {

        @Test
        @DisplayName("should store message and context")
        void shouldStoreMessageAndContext() {
            var envelope = createEnvelope("test", "ch");
            var ctx = new MessageContextBase("test");
            var pack = new MessagePack(envelope, ctx);

            assertThat(pack.getMessage()).isSameAs(envelope);
            assertThat(pack.getContext()).isSameAs(ctx);
        }

        @Test
        @DisplayName("should default aborted to false")
        void shouldDefaultAbortedToFalse() {
            var envelope = createEnvelope("msg", "ch");
            var ctx = new MessageContextBase("msg");
            var pack = new MessagePack(envelope, ctx);

            assertThat(pack.isAborted()).isFalse();
        }
    }

    @Nested
    @DisplayName("aborted flag")
    class AbortedFlag {

        @Test
        @DisplayName("should set aborted to true")
        void shouldSetAbortedToTrue() {
            var envelope = createEnvelope("msg", "ch");
            var ctx = new MessageContextBase("msg");
            var pack = new MessagePack(envelope, ctx);
            pack.setAborted(true);

            assertThat(pack.isAborted()).isTrue();
        }

        @Test
        @DisplayName("should toggle aborted flag back")
        void shouldToggleAbortedFlag() {
            var envelope = createEnvelope("msg", "ch");
            var ctx = new MessageContextBase("msg");
            var pack = new MessagePack(envelope, ctx);
            pack.setAborted(true);
            pack.setAborted(false);

            assertThat(pack.isAborted()).isFalse();
        }
    }
}
