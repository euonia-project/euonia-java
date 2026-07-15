package com.euonia.bus.consistency;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.MessageMetadata;

/**
 * 测试 {@link OutboxStore} 的默认方法。
 */
@SuppressWarnings("unused")
@DisplayName("OutboxStore")
class OutboxStoreTest {

    private final TestOutboxStore store = new TestOutboxStore();

    @AfterEach
    void tearDown() {
        store.clearCache();
    }

    @Nested
    @DisplayName("insert(message, transports)")
    class InsertWithTransports {

        @Test
        @DisplayName("should create entry with messageId channel and messageType")
        void shouldCreateEntryWithMessageIdChannelAndMessageType() {
            var envelope = stubEnvelope();

            store.insert(envelope, List.of());

            var entry = store.lastInserted();
            assertThat(entry.getMessageId()).isEqualTo("mid");
            assertThat(entry.getChannel()).isEqualTo("ch");
            assertThat(entry.getMessageType()).isEqualTo("java.lang.String");
            assertThat(entry.getContent()).isSameAs(envelope);
            assertThat(entry.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should add transports with PENDING status")
        void shouldAddTransportsWithPendingStatus() {
            var envelope = stubEnvelope();

            store.insert(envelope, List.of("kafka", "rabbitmq"));

            var entry = store.lastInserted();
            assertThat(entry.getTransports()).hasSize(2);
            assertThat(entry.getTransports().get(0).getName()).isEqualTo("kafka");
            assertThat(entry.getTransports().get(0).getStatus()).isEqualTo(OutboxTransport.Status.PENDING);
            assertThat(entry.getTransports().get(1).getName()).isEqualTo("rabbitmq");
        }

        @Test
        @DisplayName("should return true when underlying insert succeeds")
        void shouldReturnTrueWhenInsertSucceeds() {
            store.insertResult = true;
            var result = store.insert(stubEnvelope(), List.of());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when underlying insert fails")
        void shouldReturnFalseWhenInsertFails() {
            store.insertResult = false;
            var result = store.insert(stubEnvelope(), List.of());

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getAndCache")
    class GetAndCache {

        @Test
        @DisplayName("should return entry and cache it")
        void shouldReturnEntryAndCacheIt() {
            var entry = new OutboxEntry();
            entry.setMessageId("msg-1");
            store.entry = entry;

            var result1 = store.getAndCache("msg-1");
            var result2 = store.getAndCache("msg-1");

            assertThat(result1).isSameAs(entry);
            assertThat(result2).isSameAs(entry);
            assertThat(store.getCallCount).isEqualTo(1);
        }

        @Test
        @DisplayName("should return null when entry not found")
        void shouldReturnNullWhenNotFound() {
            var result = store.getAndCache("nonexistent");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("clearCache")
    class ClearCache {

        @Test
        @DisplayName("should clear cached entries")
        void shouldClearCachedEntries() {
            var entry = new OutboxEntry();
            entry.setMessageId("msg-1");
            store.entry = entry;
            store.getAndCache("msg-1");

            store.clearCache();

            assertThat(OutboxStore.CACHE).isEmpty();
        }
    }

    private static class TestOutboxStore implements OutboxStore {
        final AtomicReference<OutboxEntry> lastEntry = new AtomicReference<>();
        OutboxEntry entry;
        boolean insertResult = true;
        int getCallCount;

        OutboxEntry lastInserted() {
            return lastEntry.get();
        }

        @Override
        public boolean insert(OutboxEntry entry) {
            lastEntry.set(entry);
            return insertResult;
        }

        @Override
        public void markAsSuccess(String messageId, String transport) {
        }

        @Override
        public void markAsFailed(String messageId, String transport, String errorMessage) {
        }

        @Override
        public OutboxEntry get(String messageId) {
            getCallCount++;
            return entry;
        }

        @Override
        public List<OutboxTransport> getFailedMessages() {
            return List.of();
        }
    }

    private static MessageEnvelope<String> stubEnvelope() {
        return new MessageEnvelope<>() {
            @Override public String getMessageId() { return "mid"; }
            @Override public String getCorrelationId() { return "cid"; }
            @Override public String getConversationId() { return "convid"; }
            @Override public String getRequestTrackId() { return "tid"; }
            @Override public String getChannel() { return "ch"; }
            @Override public String getAuthorization() { return "auth"; }
            @Override public long getTimestamp() { return 1L; }
            @Override public String getPayload() { return "payload"; }
            @Override public String getTypeName() { return "OrderCreated"; }
            @Override public MessageMetadata getMetadata() { return null; }
        };
    }
}

