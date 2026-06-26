package com.euonia.bus.messenger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link StrongReferenceMessenger} 和 {@link WeakReferenceMessenger} 的注册/发送/取消注册。
 */
@SuppressWarnings({"unused", "ReassignedVariable"})
@DisplayName("Messenger")
class MessengerTest {

    @AfterEach
    void cleanup() {
        StrongReferenceMessenger.getDefault().reset();
        WeakReferenceMessenger.getDefault().reset();
    }

    @Nested
    @DisplayName("StrongReferenceMessenger")
    class StrongRef {

        @Test
        @DisplayName("should register and check registration")
        void shouldRegister() {
            var messenger = StrongReferenceMessenger.getDefault();
            var recipient = new TestRecipient();

            messenger.register(recipient, String.class, "my-channel");

            assertThat(messenger.isRegistered(recipient, String.class, "my-channel")).isTrue();
        }

        @Test
        @DisplayName("should reject duplicate registration")
        void shouldRejectDuplicate() {
            var messenger = StrongReferenceMessenger.getDefault();
            var recipient = new TestRecipient();
            messenger.register(recipient, String.class, "my-channel");

            assertThatThrownBy(() ->
                messenger.register(recipient, String.class, "my-channel"))
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should deliver message to registered recipient")
        void shouldDeliverMessage() {
            var messenger = StrongReferenceMessenger.getDefault();
            var recipient = new TestRecipient();
            messenger.register(recipient, String.class, "ch1");

            messenger.send("hello from messenger", "ch1");

            assertThat(recipient.received).isEqualTo("hello from messenger");
        }

        @Test
        @DisplayName("should return message when no recipient registered")
        void shouldReturnMessageWhenNoRecipient() {
            var messenger = StrongReferenceMessenger.getDefault();

            var result = messenger.send("no-one-listening", "empty-channel");

            assertThat(result).isEqualTo("no-one-listening");
        }

        @Test
        @DisplayName("should unregister all for a recipient")
        void shouldUnregisterAll() {
            var messenger = StrongReferenceMessenger.getDefault();
            var recipient = new TestRecipient();
            messenger.register(recipient, String.class, "ch1");
            messenger.register(recipient, Integer.class, "ch2", (r, m) -> {
            });

            messenger.unregisterAll(recipient);

            assertThat(messenger.isRegistered(recipient, String.class, "ch1")).isFalse();
            assertThat(messenger.isRegistered(recipient, Integer.class, "ch2")).isFalse();
        }

        @Test
        @DisplayName("should handle multiple recipients on same channel")
        void shouldHandleMultipleRecipients() {
            var messenger = StrongReferenceMessenger.getDefault();
            var r1 = new TestRecipient();
            var r2 = new TestRecipient();
            messenger.register(r1, String.class, "shared");
            messenger.register(r2, String.class, "shared");

            messenger.send("broadcast", "shared");

            assertThat(r1.received).isEqualTo("broadcast");
            assertThat(r2.received).isEqualTo("broadcast");
        }

        @Test
        @DisplayName("should accept MessageHandler registration")
        void shouldAcceptMessageHandler() {
            var messenger = StrongReferenceMessenger.getDefault();
            var recipient = new TestRecipient();

            messenger.register(recipient, String.class, (r, m) -> r.received = m);

            messenger.send("via-handler");

            assertThat(recipient.received).isEqualTo("via-handler");
        }
    }

    @Nested
    @DisplayName("WeakReferenceMessenger")
    class WeakRef {

        @Test
        @DisplayName("should register and deliver via weak reference")
        void shouldRegisterAndDeliver() {
            var messenger = WeakReferenceMessenger.getDefault();
            var recipient = new TestRecipient();
            messenger.register(recipient, String.class, "weak-ch");

            assertThat(messenger.isRegistered(recipient, String.class, "weak-ch")).isTrue();

            messenger.send("weak-delivery", "weak-ch");

            assertThat(recipient.received).isEqualTo("weak-delivery");
        }

        @SuppressWarnings("UnusedAssignment")
        @Test
        @DisplayName("should lose GC'd recipients")
        void shouldLoseGCdRecipients() {
            var messenger = WeakReferenceMessenger.getDefault();
            var recipient = new TestRecipient();
            messenger.register(recipient, String.class, "gc-ch");

            // Drop strong reference, force GC
            recipient = null;
            System.gc();

            // After GC, the recipient should be gone, but we can't assert
            // because GC is non-deterministic. Just verify no exception.
            messenger.send("after-gc", "gc-ch");
        }
    }

    /**
     * Simple test recipient.
     */
    static class TestRecipient implements Recipient<String> {
        String received;

        @Override
        public void receive(String message) {
            this.received = message;
        }
    }
}
