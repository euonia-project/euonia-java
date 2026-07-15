package com.euonia.bus.consistency;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.MessageMetadata;

/**
 * 测试 {@link OutboxEntry}。
 */
@SuppressWarnings("unused")
@DisplayName("OutboxEntry")
class OutboxEntryTest {

    @Nested
    @DisplayName("getter/setter")
    class GetterSetter {

        @Test
        @DisplayName("should store and return messageId")
        void shouldStoreAndReturnMessageId() {
            var entry = new OutboxEntry();
            entry.setMessageId("msg-1");

            assertThat(entry.getMessageId()).isEqualTo("msg-1");
        }

        @Test
        @DisplayName("should store and return channel with non-null non-empty value")
        void shouldStoreAndReturnChannel() {
            var entry = new OutboxEntry();
            entry.setChannel("orders");

            assertThat(entry.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should store and return messageType with non-null non-empty value")
        void shouldStoreAndReturnMessageType() {
            var entry = new OutboxEntry();
            entry.setMessageType("OrderCreated");

            assertThat(entry.getMessageType()).isEqualTo("OrderCreated");
        }

        @Test
        @DisplayName("should store and return content")
        void shouldStoreAndReturnContent() {
            var entry = new OutboxEntry();
            var envelope = stubEnvelope();
            entry.setContent(envelope);

            assertThat(entry.getContent()).isSameAs(envelope);
        }

        @Test
        @DisplayName("should allow null content")
        void shouldAllowNullContent() {
            var entry = new OutboxEntry();
            entry.setContent(null);

            assertThat(entry.getContent()).isNull();
        }

        @Test
        @DisplayName("should store and return createdAt")
        void shouldStoreAndReturnCreatedAt() {
            var entry = new OutboxEntry();
            var now = LocalDateTime.now();
            entry.setCreatedAt(now);

            assertThat(entry.getCreatedAt()).isSameAs(now);
        }

        @Test
        @DisplayName("should store and return transports list")
        void shouldStoreAndReturnTransports() {
            var entry = new OutboxEntry();
            var transports = new java.util.ArrayList<OutboxTransport>();
            var transport = new OutboxTransport();
            transport.setName("kafka");
            transports.add(transport);
            entry.setTransports(transports);

            assertThat(entry.getTransports()).isSameAs(transports).hasSize(1);
        }

        @Test
        @DisplayName("should default transports to empty list")
        void shouldDefaultTransportsToEmptyList() {
            var entry = new OutboxEntry();

            assertThat(entry.getTransports()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("null validation")
    class NullValidation {

        @Test
        @DisplayName("should throw ArgumentNullException when messageId is null")
        void shouldThrowWhenMessageIdIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setMessageId(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageId is empty")
        void shouldThrowWhenMessageIdIsEmpty() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setMessageId(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when channel is null")
        void shouldThrowWhenChannelIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setChannel(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when channel is empty")
        void shouldThrowWhenChannelIsEmpty() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setChannel(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageType is null")
        void shouldThrowWhenMessageTypeIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setMessageType(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageType is empty")
        void shouldThrowWhenMessageTypeIsEmpty() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setMessageType(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when createdAt is null")
        void shouldThrowWhenCreatedAtIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setCreatedAt(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when transports is null or empty")
        void shouldThrowWhenTransportsIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setTransports(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when transports is empty")
        void shouldThrowWhenTransportsIsEmpty() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.setTransports(new java.util.ArrayList<>()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("addTransport")
    class AddTransport {

        @Test
        @DisplayName("should add OutboxTransport to transports list")
        void shouldAddOutboxTransport() {
            var entry = new OutboxEntry();
            var transport = new OutboxTransport();
            transport.setName("kafka");

            entry.addTransport(transport);

            assertThat(entry.getTransports()).containsExactly(transport);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when OutboxTransport is null")
        void shouldThrowWhenOutboxTransportIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.addTransport((OutboxTransport) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should create and add OutboxTransport from transport name")
        void shouldCreateAndAddTransportFromName() {
            var entry = new OutboxEntry();
            entry.setMessageId("msg-1");

            entry.addTransport("kafka");

            assertThat(entry.getTransports()).hasSize(1);
            var transport = entry.getTransports().get(0);
            assertThat(transport.getName()).isEqualTo("kafka");
            assertThat(transport.getMessageId()).isEqualTo("msg-1");
        }

        @Test
        @DisplayName("should throw ArgumentNullException when transport name is null")
        void shouldThrowWhenTransportNameIsNull() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.addTransport((String) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when transport name is empty")
        void shouldThrowWhenTransportNameIsEmpty() {
            var entry = new OutboxEntry();

            assertThatThrownBy(() -> entry.addTransport(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should format with messageId and channel")
        void shouldFormatToString() {
            var entry = new OutboxEntry();
            entry.setMessageId("msg-1");
            entry.setChannel("orders");

            assertThat(entry.toString())
                    .contains("msg-1")
                    .contains("orders")
                    .startsWith("OutboxMessage{");
        }

        @Test
        @DisplayName("should include null fields in toString")
        void shouldIncludeNullFieldsInToString() {
            var entry = new OutboxEntry();

            assertThat(entry.toString())
                    .contains("messageId=null")
                    .contains("channel=null");
        }
    }

    private static MessageEnvelope<?> stubEnvelope() {
        return new MessageEnvelope<>() {
            @Override public String getMessageId() { return "mid"; }
            @Override public String getCorrelationId() { return "cid"; }
            @Override public String getConversationId() { return "convid"; }
            @Override public String getRequestTrackId() { return "tid"; }
            @Override public String getChannel() { return "ch"; }
            @Override public String getAuthorization() { return "auth"; }
            @Override public long getTimestamp() { return 1L; }
            @Override public Object getPayload() { return "payload"; }
            @Override public String getTypeName() { return "t"; }
            @Override public MessageMetadata getMetadata() { return null; }
        };
    }
}

