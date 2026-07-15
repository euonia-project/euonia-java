package com.euonia.bus.consistency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.MessageMetadata;

/**
 * 测试 {@link InboxStore} 的默认方法。
 */
@SuppressWarnings("unused")
@DisplayName("InboxStore")
class InboxStoreTest {

    private final TestInboxStore store = new TestInboxStore();

    @AfterEach
    void tearDown() {
        store.clearCache();
    }

    @Nested
    @DisplayName("insert(channel, message, handlers)")
    class InsertWithHandlers {

        @Test
        @DisplayName("should create entry with channel messageId and messageType")
        void shouldCreateEntryWithChannelMessageIdAndMessageType() {
            var envelope = stubEnvelope();

            store.insert("orders", envelope, List.of());

            var entry = store.lastInserted();
            assertThat(entry.getMessageId()).isEqualTo("mid");
            assertThat(entry.getChannel()).isEqualTo("orders");
            assertThat(entry.getMessageType()).isEqualTo("java.lang.String");
            assertThat(entry.getContent()).isSameAs(envelope);
            assertThat(entry.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should add handlers with PENDING status")
        void shouldAddHandlersWithPendingStatus() {
            var envelope = stubEnvelope();

            store.insert("orders", envelope, List.of("processor-1", "processor-2"));

            var entry = store.lastInserted();
            assertThat(entry.getHandlers()).hasSize(2);
            assertThat(entry.getHandlers().get(0).getName()).isEqualTo("processor-1");
            assertThat(entry.getHandlers().get(0).getStatus()).isEqualTo(InboxHandler.Status.PENDING);
            assertThat(entry.getHandlers().get(1).getName()).isEqualTo("processor-2");
        }

        @Test
        @DisplayName("should return true when underlying insert succeeds")
        void shouldReturnTrueWhenInsertSucceeds() {
            store.insertResult = true;
            var result = store.insert("ch", stubEnvelope(), List.of());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when underlying insert fails")
        void shouldReturnFalseWhenInsertFails() {
            store.insertResult = false;
            var result = store.insert("ch", stubEnvelope(), List.of());

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getAndCache")
    class GetAndCache {

        @Test
        @DisplayName("should return entry and cache it")
        void shouldReturnEntryAndCacheIt() {
            var entry = new InboxEntry();
            entry.setMessageId("msg-1");
            store.entry = entry;

            var result1 = store.getAndCache("msg-1");
            var result2 = store.getAndCache("msg-1");

            assertThat(result1).isSameAs(entry);
            assertThat(result2).isSameAs(entry);
            assertThat(store.getCallCount).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageId is null")
        void shouldThrowWhenMessageIdIsNull() {
            assertThatThrownBy(() -> store.getAndCache(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageId is empty")
        void shouldThrowWhenMessageIdIsEmpty() {
            assertThatThrownBy(() -> store.getAndCache(""))
                    .isInstanceOf(IllegalArgumentException.class);
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
            var entry = new InboxEntry();
            entry.setMessageId("msg-1");
            store.entry = entry;
            store.getAndCache("msg-1");

            store.clearCache();

            assertThat(InboxStore.CACHE).isEmpty();
        }
    }

    private static class TestInboxStore implements InboxStore {
        final AtomicReference<InboxEntry> lastEntry = new AtomicReference<>();
        InboxEntry entry;
        boolean insertResult = true;
        int getCallCount;

        InboxEntry lastInserted() {
            return lastEntry.get();
        }

        @Override
        public boolean insert(InboxEntry entry) {
            lastEntry.set(entry);
            return insertResult;
        }

        @Override
        public void markAsSuccess(String messageId, String handler) {
        }

        @Override
        public void markAsFailed(String messageId, String handler, String errorMessage) {
        }

        @Override
        public InboxEntry get(String messageId) {
            getCallCount++;
            return entry;
        }

        @Override
        public List<InboxHandler> getFailedMessages() {
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

