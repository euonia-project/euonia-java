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
 * 测试 {@link InboxEntry}。
 */
@SuppressWarnings("unused")
@DisplayName("InboxEntry")
class InboxEntryTest {

    @Nested
    @DisplayName("getter/setter")
    class GetterSetter {

        @Test
        @DisplayName("should store and return messageId")
        void shouldStoreAndReturnMessageId() {
            var entry = new InboxEntry();
            entry.setMessageId("msg-1");

            assertThat(entry.getMessageId()).isEqualTo("msg-1");
        }

        @Test
        @DisplayName("should store and return channel with non-null non-empty value")
        void shouldStoreAndReturnChannel() {
            var entry = new InboxEntry();
            entry.setChannel("orders");

            assertThat(entry.getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should store and return messageType with non-null non-empty value")
        void shouldStoreAndReturnMessageType() {
            var entry = new InboxEntry();
            entry.setMessageType("OrderCreated");

            assertThat(entry.getMessageType()).isEqualTo("OrderCreated");
        }

        @Test
        @DisplayName("should store and return content")
        void shouldStoreAndReturnContent() {
            var entry = new InboxEntry();
            var envelope = stubEnvelope();
            entry.setContent(envelope);

            assertThat(entry.getContent()).isSameAs(envelope);
        }

        @Test
        @DisplayName("should store and return createdAt")
        void shouldStoreAndReturnCreatedAt() {
            var entry = new InboxEntry();
            var now = LocalDateTime.now();
            entry.setCreatedAt(now);

            assertThat(entry.getCreatedAt()).isSameAs(now);
        }

        @Test
        @DisplayName("should store and return handles list")
        void shouldStoreAndReturnHandles() {
            var entry = new InboxEntry();
            var handles = new java.util.ArrayList<InboxHandler>();
            var handle = new InboxHandler();
            handle.setName("handler-1");
            handles.add(handle);
            entry.setHandlers(handles);

            assertThat(entry.getHandlers()).isSameAs(handles).hasSize(1);
        }

        @Test
        @DisplayName("should default handles to empty list")
        void shouldDefaultHandlesToEmptyList() {
            var entry = new InboxEntry();

            assertThat(entry.getHandlers()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("null validation")
    class NullValidation {

        @Test
        @DisplayName("should throw ArgumentNullException when channel is null")
        void shouldThrowWhenChannelIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setChannel(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when channel is empty")
        void shouldThrowWhenChannelIsEmpty() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setChannel(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageType is null")
        void shouldThrowWhenMessageTypeIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setMessageType(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when messageType is empty")
        void shouldThrowWhenMessageTypeIsEmpty() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setMessageType(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when content is null")
        void shouldThrowWhenContentIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setContent(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when createdAt is null")
        void shouldThrowWhenCreatedAtIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setCreatedAt(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when handles is null")
        void shouldThrowWhenHandlesIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.setHandlers(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should not throw when messageId is null")
        void shouldNotThrowWhenMessageIdIsNull() {
            var entry = new InboxEntry();
            entry.setMessageId(null);

            assertThat(entry.getMessageId()).isNull();
        }
    }

    @Nested
    @DisplayName("addHandle")
    class AddHandle {

        @Test
        @DisplayName("should add InboxHandle to handles list")
        void shouldAddInboxHandle() {
            var entry = new InboxEntry();
            var handle = new InboxHandler();
            handle.setName("h1");

            entry.addHandler(handle);

            assertThat(entry.getHandlers()).containsExactly(handle);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when InboxHandle is null")
        void shouldThrowWhenInboxHandleIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.addHandler((InboxHandler) null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should create and add InboxHandle from handler name")
        void shouldCreateAndAddHandleFromHandlerName() {
            var entry = new InboxEntry();
            entry.setMessageId("msg-1");

            entry.addHandler("processor-1");

            assertThat(entry.getHandlers()).hasSize(1);
            var handle = entry.getHandlers().get(0);
            assertThat(handle.getName()).isEqualTo("processor-1");
            assertThat(handle.getMessageId()).isEqualTo("msg-1");
        }

        @Test
        @DisplayName("should throw ArgumentNullException when handler name is null")
        void shouldThrowWhenHandlerNameIsNull() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.addHandler((String) null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw ArgumentNullException when handler name is empty")
        void shouldThrowWhenHandlerNameIsEmpty() {
            var entry = new InboxEntry();

            assertThatThrownBy(() -> entry.addHandler(""))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should format with channel, messageId and messageType")
        void shouldFormatToString() {
            var entry = new InboxEntry();
            entry.setChannel("orders");
            entry.setMessageId("msg-1");
            entry.setMessageType("OrderCreated");

            assertThat(entry.toString())
                .contains("channel=orders")
                .contains("messageId=msg-1")
                .contains("messageType=OrderCreated");
        }

        @Test
        @DisplayName("should include null fields in toString")
        void shouldIncludeNullFieldsInToString() {
            var entry = new InboxEntry();

            assertThat(entry.toString())
                .contains("channel=null")
                .contains("messageId=null")
                .contains("messageType=null");
        }
    }

    private static MessageEnvelope<?> stubEnvelope() {
        return new MessageEnvelope<>() {
            @Override
            public String getMessageId() {
                return "mid";
            }

            @Override
            public String getCorrelationId() {
                return "cid";
            }

            @Override
            public String getConversationId() {
                return "convid";
            }

            @Override
            public String getRequestTrackId() {
                return "tid";
            }

            @Override
            public String getChannel() {
                return "ch";
            }

            @Override
            public String getAuthorization() {
                return "auth";
            }

            @Override
            public long getTimestamp() {
                return 1L;
            }

            @Override
            public Object getPayload() {
                return "payload";
            }

            @Override
            public String getTypeName() {
                return "t";
            }

            @Override
            public MessageMetadata getMetadata() {
                return null;
            }
        };
    }
}

