package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.dlq.DeadLetterMessage;

/**
 * 测试 {@link InMemoryDeadLetterQueue}。
 */
@SuppressWarnings("unused")
@DisplayName("InMemoryDeadLetterQueue")
class InMemoryDeadLetterQueueTest {

    @AfterEach
    void cleanup() {
        InMemoryDeadLetterQueue.getInstance().reset();
    }

    @SuppressWarnings("unchecked")
    private static DeadLetterMessage<String> createDeadLetter(String payload, String reason) {
        MessageEnvelope<String> envelope = new MessageEnvelope<>() {
            @Override public String getMessageId() { return "id-" + payload; }
            @Override public String getCorrelationId() { return null; }
            @Override public String getConversationId() { return null; }
            @Override public String getRequestTrackId() { return null; }
            @Override public String getChannel() { return "test-channel"; }
            @Override public String getAuthorization() { return null; }
            @Override public long getTimestamp() { return System.currentTimeMillis(); }
            @Override public String getPayload() { return payload; }
            @Override public String getTypeName() { return String.class.getName(); }
            @Override public MessageMetadata getMetadata() { return new MessageMetadata(); }
        };
        return new DeadLetterMessage<>(envelope, new RuntimeException(reason));
    }

    @Nested
    @DisplayName("singleton")
    class Singleton {

        @Test
        @DisplayName("should return same instance")
        void shouldReturnSameInstance() {
            var a = InMemoryDeadLetterQueue.getInstance();
            var b = InMemoryDeadLetterQueue.getInstance();

            assertThat(a).isSameAs(b);
        }
    }

    @Nested
    @DisplayName("publish and query")
    class PublishAndQuery {

        @Test
        @DisplayName("should publish dead letter to channel")
        void shouldPublishDeadLetterToChannel() {
            var dlq = InMemoryDeadLetterQueue.getInstance();
            var dl = createDeadLetter("failed-msg", "processing error");

            dlq.publish("orders", dl);

            var letters = dlq.getDeadLetters("orders");
            assertThat(letters).hasSize(1);
            assertThat(letters.get(0).getReason()).isEqualTo("processing error");
        }

        @Test
        @DisplayName("should return empty list for unknown channel")
        void shouldReturnEmptyListForUnknownChannel() {
            var dlq = InMemoryDeadLetterQueue.getInstance();

            var letters = dlq.getDeadLetters("nonexistent");

            assertThat(letters).isEmpty();
        }

        @Test
        @DisplayName("should accumulate multiple dead letters")
        void shouldAccumulateMultipleDeadLetters() {
            var dlq = InMemoryDeadLetterQueue.getInstance();
            dlq.publish("ch1", createDeadLetter("msg1", "reason1"));
            dlq.publish("ch1", createDeadLetter("msg2", "reason2"));

            assertThat(dlq.getDeadLetters("ch1")).hasSize(2);
        }

        @Test
        @DisplayName("should separate letters by channel")
        void shouldSeparateLettersByChannel() {
            var dlq = InMemoryDeadLetterQueue.getInstance();
            dlq.publish("ch1", createDeadLetter("a", "reason-a"));
            dlq.publish("ch2", createDeadLetter("b", "reason-b"));

            assertThat(dlq.getDeadLetters("ch1")).hasSize(1);
            assertThat(dlq.getDeadLetters("ch2")).hasSize(1);
        }
    }

    @Nested
    @DisplayName("size")
    class Size {

        @Test
        @DisplayName("should be zero initially")
        void shouldBeZeroInitially() {
            var dlq = InMemoryDeadLetterQueue.getInstance();

            assertThat(dlq.size()).isZero();
        }

        @Test
        @DisplayName("should count across all channels")
        void shouldCountAcrossAllChannels() {
            var dlq = InMemoryDeadLetterQueue.getInstance();
            dlq.publish("a", createDeadLetter("1", "r1"));
            dlq.publish("a", createDeadLetter("2", "r2"));
            dlq.publish("b", createDeadLetter("3", "r3"));

            assertThat(dlq.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("should clear all dead letters")
        void shouldClearAllDeadLetters() {
            var dlq = InMemoryDeadLetterQueue.getInstance();
            dlq.publish("ch", createDeadLetter("msg", "reason"));

            dlq.reset();

            assertThat(dlq.size()).isZero();
            assertThat(dlq.getDeadLetters("ch")).isEmpty();
        }
    }
}
