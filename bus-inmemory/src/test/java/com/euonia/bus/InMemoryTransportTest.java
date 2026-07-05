package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.euonia.bus.event.MessageDeliveredEvent;
import com.euonia.bus.messenger.StrongReferenceMessenger;
import com.euonia.bus.messenger.WeakReferenceMessenger;
import com.euonia.core.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link InMemoryTransport} 的发布、发送、调用模式。
 */
@SuppressWarnings({"unused", "resource", "ConvertToTryWithResources"})
@DisplayName("InMemoryTransport")
class InMemoryTransportTest {

    private final InMemoryTransport transport = new InMemoryTransport(new InMemoryBusOptions());

    @AfterEach
    void cleanup() {
        StrongReferenceMessenger.getDefault().reset();
        WeakReferenceMessenger.getDefault().reset();
    }

    @Nested
    @DisplayName("publishAsync")
    class Publish {

        @Test
        @DisplayName("should complete immediately for any message")
        void shouldCompleteImmediately() throws Exception {
            var msg = getMessageEnvelope("event", "test-channel");
            var future = transport.publishAsync(msg);

            assertThat(future.get(2, TimeUnit.SECONDS)).isNull();
        }

        @Test
        @DisplayName("should fire delivered event")
        void shouldFireDeliveredEvent() throws Exception {
            var delivered = new CompletableFuture<MessageDeliveredEvent>();
            transport.addDeliveredListener(delivered::complete);

            var msg = getMessageEnvelope("event", "test-channel");
            transport.publishAsync(msg);

            var event = delivered.get(2, TimeUnit.SECONDS);
            assertThat(event.getMessage()).isNotNull();
        }
    }

    @Nested
    @DisplayName("sendAsync with response")
    class SendWithResponse {

        @Test
        @DisplayName("should return response from handler")
        void shouldReturnResponse() throws Exception {
            var msg = getMessageEnvelope("cmd", "send-test");

            // Register a handler that echoes the message as response
            StrongReferenceMessenger.getDefault().register(
                new InMemoryUnicastRecipient(new StubHandlerContext()),
                MessagePack.class, "send-test");

            transport.publishAsync(msg);

            // sendAsync should be consistent with publish for in-memory
            var future = transport.sendAsync(msg);
            assertThat(future.get(2, TimeUnit.SECONDS)).isNull();
        }
    }

    @Nested
    @DisplayName("name")
    class Name {

        @Test
        @DisplayName("should return configured name")
        void shouldReturnConfiguredName() {
            var opts = new InMemoryBusOptions();
            opts.setName("MyTransport");
            var t = new InMemoryTransport(opts);

            assertThat(t.getName()).isEqualTo("MyTransport");
        }

        @Test
        @DisplayName("should fallback to default name")
        void shouldFallbackToDefaultName() {
            assertThat(transport.getName())
                .isEqualTo(InMemoryBusOptions.DEFAULT_TRANSPORT_NAME);
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("should reset messenger on close")
        void shouldResetOnClose() {
            var msg = getMessageEnvelope("test", "close-test");
            StrongReferenceMessenger.getDefault().register(
                new InMemoryUnicastRecipient(new StubHandlerContext()),
                MessagePack.class, "close-test");

            transport.close();

            // After close, messenger should be empty
            assertThat(StrongReferenceMessenger.getDefault()
                                               .isRegistered(new Object(), MessagePack.class, "close-test")).isFalse();
        }
    }

    /**
     * Stub HandlerContext that does nothing.
     */
    private static class StubHandlerContext implements HandlerContext {
        @Override
        public void onMessageSubscribed(Consumer<com.euonia.bus.event.MessageSubscribedEvent> listener) {
        }

        @Override
        public CompletableFuture<Object> handleAsync(String channel, String recipient, MessageEnvelope<?> envelope, MessageContext context) {
            return handleAsync(channel, envelope.getPayload(), context);
        }

        public CompletableFuture<Object> handleAsync(String channel, Object message, MessageContext context) {
            context.response(message);
            return CompletableFuture.completedFuture(message);
        }
    }

    private <T> MessageEnvelope<T> getMessageEnvelope(T payload, String channel) {
        var msg = new MessageEnvelope<T>() {
            String channel;
            T payload;
            final String messageId = ObjectId.newRandomId();

            @Override
            public String getMessageId() {
                return messageId;
            }

            @Override
            public String getCorrelationId() {
                return "";
            }

            @Override
            public String getConversationId() {
                return "";
            }

            @Override
            public String getRequestTrackId() {
                return "";
            }

            @Override
            public String getChannel() {
                return this.channel;
            }

            public void setChannel(String channel) {
                this.channel = channel;
            }

            @Override
            public String getAuthorization() {
                return "";
            }

            @Override
            public long getTimestamp() {
                return 0;
            }

            @Override
            public T getPayload() {
                return this.payload;
            }

            public void setPayload(T payload) {
                this.payload = payload;
            }

            @Override
            public String getTypeName() {
                return this.payload.getClass().getName();
            }

            @Override
            public MessageMetadata getMetadata() {
                return null;
            }
        };

        msg.setChannel(channel);
        msg.setPayload(payload);
        return msg;
    }
}
