package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.euonia.core.ArgumentNullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link RoutedMessage} 核心功能。
 */
@SuppressWarnings("unused")
@DisplayName("RoutedMessage")
class RoutedMessageTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should throw when payload is null")
        void shouldThrowWhenPayloadIsNull() {
            assertThatThrownBy(() -> new RoutedMessage<>(null, "orders"))
                .isInstanceOf(ArgumentNullException.class)
                .hasMessageContaining("payload");
        }

        @Test
        @DisplayName("should accept any reference type including autoboxed primitives")
        void shouldAcceptAnyReferenceType() {
            var msg = new RoutedMessage<>(42, "orders");

            assertThat(msg.getPayload()).isEqualTo(42);
            assertThat(msg.getMessageId()).isNotNull();
        }

        @Test
        @DisplayName("should auto-generate ids and timestamp on creation")
        void shouldAutoGenerateIds() {
            var msg = new RoutedMessage<>(new TestPayload("payload"), "orders");

            assertThat(msg.getMessageId()).isNotNull();
            assertThat(msg.getTimestamp()).isGreaterThan(0);
            assertThat(msg.getPayload().getField()).isEqualTo("payload");
            assertThat(msg.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should accept explicit message id")
        void shouldAcceptExplicitId() {
            var msg = new RoutedMessage<>(new TestPayload("payload"), "orders", "msg-123");

            assertThat(msg.getMessageId()).isEqualTo("msg-123");
            assertThat(msg.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should store payload class as type name in metadata")
        void shouldStoreTypeName() {
            var msg = new RoutedMessage<>(new TestPayload("hello"), "ch");

            assertThat(msg.getTypeName()).isEqualTo("com.euonia.bus.RoutedMessageTest$TestPayload");
        }

        @Test
        @DisplayName("should not have type name when payload is null")
        void shouldNotStoreTypeNameForNullPayload() {
            var msg = new RoutedMessage<>();

            assertThat(msg.getTypeName()).isNull();
        }
    }

    @Nested
    @DisplayName("metadata")
    class Metadata {

        @Test
        @DisplayName("should store and retrieve typed metadata")
        void shouldStoreTypedMetadata() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setMetadata("priority", 5);

            assertThat(msg.<Integer>getMetadata("priority", Integer.class)).isEqualTo(5);
        }

        @Test
        @DisplayName("should return null for missing key")
        void shouldReturnNullForMissingKey() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThat(msg.getMetadata("no-such-key")).isNull();
        }

        @Test
        @DisplayName("should throw for wrong type")
        void shouldThrowForWrongType() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setMetadata("key", "string-value");

            assertThatThrownBy(() -> msg.getMetadata("key", Integer.class))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("should update request track id")
        void shouldUpdateRequestTrackId() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setRequestTrackId("trace-1");

            assertThat(msg.getRequestTrackId()).isEqualTo("trace-1");
        }

        @Test
        @DisplayName("should update authorization")
        void shouldUpdateAuthorization() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setAuthorization("Bearer token");

            assertThat(msg.getAuthorization()).isEqualTo("Bearer token");
        }

        @Test
        @DisplayName("should update correlation id")
        void shouldUpdateCorrelationId() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setCorrelationId("corr-1");

            assertThat(msg.getCorrelationId()).isEqualTo("corr-1");
        }

        @Test
        @DisplayName("should update timestamp explicitly")
        void shouldUpdateTimestamp() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setTimestamp(1000L);

            assertThat(msg.getTimestamp()).isEqualTo(1000L);
        }
    }

    @Test
    @DisplayName("should produce meaningful toString")
    void shouldProduceMeaningfulToString() {
        var msg = new RoutedMessage<>(new TestPayload("payload"), "orders");

        assertThat(msg.toString()).contains(msg.getMessageId());
    }

    static class TestPayload {
        private String field;

        public TestPayload(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    @Nested
    @DisplayName("conversationId")
    class ConversationId {

        @Test
        @DisplayName("should update conversation id")
        void shouldUpdateConversationId() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setConversationId("conv-1");

            assertThat(msg.getConversationId()).isEqualTo("conv-1");
        }
    }

    @Nested
    @DisplayName("setter validation")
    class SetterValidation {

        @Test
        @DisplayName("should throw when conversationId is null")
        void shouldThrowWhenConversationIdIsNull() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setConversationId(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when conversationId is empty")
        void shouldThrowWhenConversationIdIsEmpty() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setConversationId(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when requestTrackId is null")
        void shouldThrowWhenRequestTrackIdIsNull() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setRequestTrackId(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when requestTrackId is empty")
        void shouldThrowWhenRequestTrackIdIsEmpty() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setRequestTrackId(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when channel is null")
        void shouldThrowWhenChannelIsNull() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setChannel(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when channel is empty")
        void shouldThrowWhenChannelIsEmpty() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setChannel(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when typeName is null")
        void shouldThrowWhenTypeNameIsNull() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setTypeName(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when typeName is empty")
        void shouldThrowWhenTypeNameIsEmpty() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThatThrownBy(() -> msg.setTypeName(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("typeName")
    class TypeName {

        @Test
        @DisplayName("should update typeName explicitly")
        void shouldUpdateTypeName() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setTypeName("com.example.MyMessage");

            assertThat(msg.getTypeName()).isEqualTo("com.example.MyMessage");
        }
    }

    @Nested
    @DisplayName("raw metadata")
    class RawMetadata {

        @Test
        @DisplayName("should return metadata container")
        void shouldReturnMetadataContainer() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");

            assertThat(msg.getMetadata()).isNotNull();
        }

        @Test
        @DisplayName("should store and retrieve raw metadata by key")
        void shouldStoreAndRetrieveRawMetadata() {
            var msg = new RoutedMessage<>(new TestPayload("p"), "c");
            msg.setMetadata("custom-key", "custom-value");

            assertThat(msg.getMetadata("custom-key")).isEqualTo("custom-value");
        }
    }

    @Nested
    @DisplayName("default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("should allow setting fields after default construction")
        void shouldAllowSettingFieldsAfterDefaultConstruction() {
            var msg = new RoutedMessage<String>();
            msg.setPayload("hello");
            msg.setChannel("orders");
            msg.setMessageId("id-1");
            msg.setConversationId("conv-1");
            msg.setRequestTrackId("trace-1");
            msg.setAuthorization("Bearer abc");
            msg.setTimestamp(999L);

            assertThat(msg.getPayload()).isEqualTo("hello");
            assertThat(msg.getChannel()).isEqualTo("orders");
            assertThat(msg.getMessageId()).isEqualTo("id-1");
            assertThat(msg.getConversationId()).isEqualTo("conv-1");
            assertThat(msg.getRequestTrackId()).isEqualTo("trace-1");
            assertThat(msg.getAuthorization()).isEqualTo("Bearer abc");
            assertThat(msg.getTimestamp()).isEqualTo(999L);
        }
    }
}
