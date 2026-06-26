package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link RoutedMessage} 核心功能。
 */
@DisplayName("RoutedMessage")
class RoutedMessageTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should auto-generate ids and timestamp on creation")
        void shouldAutoGenerateIds() {
            var msg = new RoutedMessage<>("payload", "orders");

            assertThat(msg.getMessageId()).isNotNull();
            assertThat(msg.getCorrelationId()).isNotNull();
            assertThat(msg.getConversationId()).isNotNull();
            assertThat(msg.getTimestamp()).isGreaterThan(0);
            assertThat(msg.getPayload()).isEqualTo("payload");
            assertThat(msg.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should accept explicit message id")
        void shouldAcceptExplicitId() {
            var msg = new RoutedMessage<>("payload", "orders", "msg-123");

            assertThat(msg.getMessageId()).isEqualTo("msg-123");
            assertThat(msg.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should store payload class as type name in metadata")
        void shouldStoreTypeName() {
            var msg = new RoutedMessage<>("hello", "ch");

            assertThat(msg.getTypeName()).isEqualTo("java.lang.String");
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
            var msg = new RoutedMessage<>("p", "c");
            msg.setMetadata("priority", 5);

            assertThat(msg.<Integer>getMetadata("priority", Integer.class)).isEqualTo(5);
        }

        @Test
        @DisplayName("should return null for missing key")
        void shouldReturnNullForMissingKey() {
            var msg = new RoutedMessage<>("p", "c");

            assertThat(msg.getMetadata("no-such-key")).isNull();
        }

        @Test
        @DisplayName("should throw for wrong type")
        void shouldThrowForWrongType() {
            var msg = new RoutedMessage<>("p", "c");
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
            var msg = new RoutedMessage<>("p", "c");
            msg.setRequestTrackId("trace-1");

            assertThat(msg.getRequestTrackId()).isEqualTo("trace-1");
        }

        @Test
        @DisplayName("should update authorization")
        void shouldUpdateAuthorization() {
            var msg = new RoutedMessage<>("p", "c");
            msg.setAuthorization("Bearer token");

            assertThat(msg.getAuthorization()).isEqualTo("Bearer token");
        }

        @Test
        @DisplayName("should update correlation id")
        void shouldUpdateCorrelationId() {
            var msg = new RoutedMessage<>("p", "c");
            msg.setCorrelationId("corr-1");

            assertThat(msg.getCorrelationId()).isEqualTo("corr-1");
        }

        @Test
        @DisplayName("should update timestamp explicitly")
        void shouldUpdateTimestamp() {
            var msg = new RoutedMessage<>("p", "c");
            msg.setTimestamp(1000L);

            assertThat(msg.getTimestamp()).isEqualTo(1000L);
        }
    }

    @Test
    @DisplayName("should produce meaningful toString")
    void shouldProduceMeaningfulToString() {
        var msg = new RoutedMessage<>("payload", "orders");

        assertThat(msg.toString()).contains(msg.getMessageId());
    }
}
