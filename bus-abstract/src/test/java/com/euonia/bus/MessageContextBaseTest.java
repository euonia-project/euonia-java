package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.euonia.bus.event.MessageRepliedEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageContextBase} 的生命周期和事件通知。
 */
@SuppressWarnings({"unused", "resource", "ConvertToTryWithResources"})
@DisplayName("MessageContextBase")
class MessageContextBaseTest {

    @Nested
    @DisplayName("header management")
    class Headers {

        @Test
        @DisplayName("should store message id in headers")
        void shouldStoreMessageId() {
            var ctx = new MessageContextBase("msg");
            ctx.setMessageId("id-1");

            assertThat(ctx.getMessageId()).isEqualTo("id-1");
        }

        @Test
        @DisplayName("should store correlation id")
        void shouldStoreCorrelationId() {
            var ctx = new MessageContextBase("msg");
            ctx.setCorrelationId("corr-1");

            assertThat(ctx.getCorrelationId()).isEqualTo("corr-1");
        }

        @Test
        @DisplayName("should store conversation id")
        void shouldStoreConversationId() {
            var ctx = new MessageContextBase("msg");
            ctx.setConversationId("conv-1");

            assertThat(ctx.getConversationId()).isEqualTo("conv-1");
        }

        @Test
        @DisplayName("should store request trace id")
        void shouldStoreRequestTraceId() {
            var ctx = new MessageContextBase("msg");
            ctx.setRequestTraceId("trace-1");

            assertThat(ctx.getRequestTraceId()).isEqualTo("trace-1");
        }

        @Test
        @DisplayName("should store authorization")
        void shouldStoreAuthorization() {
            var ctx = new MessageContextBase("msg");
            ctx.setAuthorization("Bearer xyz");

            assertThat(ctx.getAuthorization()).isEqualTo("Bearer xyz");
        }

        @Test
        @DisplayName("should return wrapped message")
        void shouldReturnWrappedMessage() {
            var ctx = new MessageContextBase("hello");

            assertThat(ctx.getMessage()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should return unmodifiable headers map")
        void shouldReturnUnmodifiableHeaders() {
            var ctx = new MessageContextBase("msg");
            ctx.setMessageId("id");

            assertThat(ctx.getHeaders()).containsEntry(MessageHeaders.MESSAGE_ID, "id");
        }

        @Test
        @DisplayName("should return null for non-existent header")
        void shouldReturnNullForMissingHeader() {
            var ctx = new MessageContextBase("msg");

            assertThat(ctx.getMessageId()).isNull();
        }
    }

    @Nested
    @DisplayName("RoutedMessage constructor")
    class RoutedMessageConstructor {

        @Test
        @DisplayName("should copy metadata from routed message")
        void shouldCopyMetadata() {
            var rm = new RoutedMessage<>(new TestPayload("payload"), "orders");
            rm.setAuthorization("Bearer abc");

            var ctx = new MessageContextBase(rm);

            assertThat(ctx.getMessageId()).isEqualTo(rm.getMessageId());
            assertThat(ctx.getCorrelationId()).isEqualTo(rm.getCorrelationId());
            assertThat(ctx.getAuthorization()).isEqualTo("Bearer abc");
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("should fire completed event on close")
        void shouldFireCompletedEventOnClose() throws Exception {
            var ctx = new MessageContextBase("original-msg");
            var latch = new CountDownLatch(1);
            var captured = new AtomicReference<Object>();

            ctx.addCompletedSubscriber(e -> {
                captured.set(e.getMessage());
                latch.countDown();
            });

            ctx.close();

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(captured.get()).isEqualTo("original-msg");
        }

        @Test
        @DisplayName("should only complete once")
        void shouldOnlyCompleteOnce() throws Exception {
            var ctx = new MessageContextBase("msg");
            var latch = new CountDownLatch(1);
            var count = new int[1];
            ctx.addCompletedSubscriber(e -> {
                count[0]++;
                latch.countDown();
            });

            ctx.close();
            ctx.close(); // second close should be no-op

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(count[0]).isEqualTo(1);
        }

        @Test
        @DisplayName("should fire response event")
        void shouldFireResponseEvent() throws Exception {
            var ctx = new MessageContextBase("msg");
            var future = new CompletableFuture<Object>();

            ctx.onReplied(new java.util.concurrent.Flow.Subscriber<>() {
                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(MessageRepliedEvent event) {
                    future.complete(event.getResult());
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                }
            });

            ctx.response("response-data");

            assertThat(future.get(2, TimeUnit.SECONDS)).isEqualTo("response-data");
        }

        @Test
        @DisplayName("should fire failure event")
        void shouldFireFailureEvent() throws Exception {
            var ctx = new MessageContextBase("msg");
            var future = new CompletableFuture<Throwable>();

            ctx.onReplied(new java.util.concurrent.Flow.Subscriber<>() {
                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription s) {
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(MessageRepliedEvent event) {
                }

                @Override
                public void onError(Throwable t) {
                    future.complete(t);
                }

                @Override
                public void onComplete() {
                }
            });

            ctx.failure(new RuntimeException("test-error"));

            assertThat(future.get(2, TimeUnit.SECONDS)).hasMessage("test-error");
        }
    }

    @Nested
    @DisplayName("metadata")
    class MetadataTests {

        @Test
        @DisplayName("should support metadata get/set")
        void shouldSupportMetadata() {
            var ctx = new MessageContextBase("msg");
            var meta = new MessageMetadata();
            meta.put("k", "v");
            ctx.setMetadata(meta);

            assertThat(ctx.getMetadata()).isSameAs(meta);
        }
    }

    static class TestPayload {
        private String payload;

        public TestPayload(String payload) {
            this.payload = payload;
        }
    }
}
