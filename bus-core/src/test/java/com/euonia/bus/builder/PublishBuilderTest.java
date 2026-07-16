package com.euonia.bus.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.euonia.bus.MessageEnvelope;
import com.euonia.core.ArgumentNullException;
import com.euonia.pipeline.Pipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.Bus;
import com.euonia.bus.options.PublishOptions;

/**
 * 测试 {@link PublishBuilder} 的链式构造和委托行为。
 */
@DisplayName("PublishBuilder")
class PublishBuilderTest {

    /**
     * 创建一个委托给 Mock Bus 的 builder，用于断言 runAsync 传递的参数。
     */
    private static PublishBuilder<String> builderFor(String message, AtomicReference<String> capturedMessage,
                                                      AtomicReference<PublishOptions> capturedOptions,
                                                      AtomicReference<Consumer<?>> capturedBehavior) {
        Bus bus = new Bus() {
            @Override
            public <T> CompletableFuture<Void> publishAsync(T msg, PublishOptions opts, Consumer<Pipeline<MessageEnvelope<T>, Void>> behavior) {
                capturedMessage.set((String) msg);
                capturedOptions.set(opts);
                capturedBehavior.set(behavior);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, java.util.concurrent.Flow.Subscriber<R> callback,
                                                             com.euonia.bus.options.SendOptions sendOptions,
                                                             Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, com.euonia.bus.options.CallOptions callOptions,
                                                          Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
                return CompletableFuture.completedFuture(null);
            }
        };
        return new PublishBuilder<>(bus, message);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create builder via Bus.publish()")
        void shouldCreateViaBusPublish() {
            Bus bus = new Bus() {
                @Override
                public <T> CompletableFuture<Void> publishAsync(T message, PublishOptions options, Consumer<Pipeline<MessageEnvelope<T>, Void>> behavior) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, java.util.concurrent.Flow.Subscriber<R> callback,
                                                                 com.euonia.bus.options.SendOptions sendOptions,
                                                                 Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, com.euonia.bus.options.CallOptions callOptions,
                                                              Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
                    return CompletableFuture.completedFuture(null);
                }
            };

            var builder = bus.publish("hello");
            assertThat(builder).isNotNull();
            assertThat(builder).isInstanceOf(PublishBuilder.class);
        }
    }

    @Nested
    @DisplayName("chain configuration")
    class ChainConfiguration {

        @Test
        @DisplayName("should set channel on options")
        void shouldSetChannel() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withChannel("orders").executeAsync();

            assertThat(capturedOptions.get().getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should configure behavior callback")
        void shouldConfigureBehavior() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);

            Consumer<Pipeline<MessageEnvelope<String>, Void>> behavior = pm -> {};
            builder.withBehavior(behavior).executeAsync();

            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }

        @Test
        @DisplayName("should configure metadata setter")
        void shouldConfigureMetadata() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withMetadata(m -> m.put("key", "value")).executeAsync();

            assertThat(capturedOptions.get().getMetadataSetter()).isNotNull();
        }

        @Test
        @DisplayName("should set custom message ID")
        void shouldSetMessageId() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withMessageId("custom-id-123").executeAsync();

            assertThat(capturedOptions.get().getMessageId()).isEqualTo("custom-id-123");
        }

        @Test
        @DisplayName("should set priority")
        void shouldSetPriority() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withPriority(5).executeAsync();

            assertThat(capturedOptions.get().getPriority()).isEqualTo(5);
        }

        @Test
        @DisplayName("should set timeout")
        void shouldSetTimeout() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withTimeout(3000L).executeAsync();

            assertThat(capturedOptions.get().getTimeout()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("should set delay")
        void shouldSetDelay() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withDelay(500L).executeAsync();

            assertThat(capturedOptions.get().getDelay()).isEqualTo(500L);
        }

        @Test
        @DisplayName("should chain multiple options correctly")
        void shouldChainMultipleOptions() {
            var capturedMessage = new AtomicReference<String>();
            var capturedOptions = new AtomicReference<PublishOptions>();
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("event", capturedMessage, capturedOptions, capturedBehavior);

            Consumer<Pipeline<MessageEnvelope<String>, Void>> behavior = pm -> {};
            builder.withChannel("notifications")
                   .withMessageId("msg-001")
                   .withPriority(10)
                   .withTimeout(5000L)
                   .withDelay(200L)
                   .withBehavior(behavior)
                   .executeAsync();

            assertThat(capturedMessage.get()).isEqualTo("event");
            assertThat(capturedOptions.get().getChannel()).isEqualTo("notifications");
            assertThat(capturedOptions.get().getMessageId()).isEqualTo("msg-001");
            assertThat(capturedOptions.get().getPriority()).isEqualTo(10);
            assertThat(capturedOptions.get().getTimeout()).isEqualTo(5000L);
            assertThat(capturedOptions.get().getDelay()).isEqualTo(200L);
            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }
    }

    @Nested
    @DisplayName("runAsync")
    class RunAsync {

        @Test
        @DisplayName("should return completed future when bus completes")
        void shouldReturnCompletedFuture() {
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>());

            var future = builder.executeAsync();

            assertThat(future).isCompleted();
        }

        @Test
        @DisplayName("should pass message to bus.publishAsync")
        void shouldPassMessageToBus() {
            var capturedMessage = new AtomicReference<String>();
            var builder = builderFor("test-message", capturedMessage, new AtomicReference<>(), new AtomicReference<>());

            builder.executeAsync();

            assertThat(capturedMessage.get()).isEqualTo("test-message");
        }

        @Test
        @DisplayName("should pass PublishOptions to bus.publishAsync")
        void shouldPassPublishOptions() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.executeAsync();

            assertThat(capturedOptions.get()).isNotNull();
            assertThat(capturedOptions.get()).isInstanceOf(PublishOptions.class);
        }

        @Test
        @DisplayName("should pass null behavior when not configured")
        void shouldPassNullBehaviorByDefault() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);

            builder.executeAsync();

            assertThat(capturedBehavior.get()).isNull();
        }
    }

    @Nested
    @DisplayName("null safety")
    class NullSafety {

        @Test
        @DisplayName("should not accept null channel")
        void shouldNotAcceptNullChannel() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            assertThatThrownBy(() -> builder.withChannel(null).executeAsync())
                    .isInstanceOf(ArgumentNullException.class)
                    .hasMessageContaining("channel");
        }

        @Test
        @DisplayName("should not accept null behavior")
        void shouldNotAcceptNullBehavior() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);

            assertThatThrownBy(() -> builder.withBehavior(null).executeAsync())
                    .isInstanceOf(ArgumentNullException.class)
                    .hasMessageContaining("behavior");

            assertThat(capturedBehavior.get()).isNull();
        }

        @Test
        @DisplayName("should not accept null metadata setter")
        void shouldNotAcceptNullMetadata() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            assertThatThrownBy(() -> builder.withMetadata(null).executeAsync())
                    .isInstanceOf(ArgumentNullException.class)
                    .hasMessageContaining("metadataSetter");
        }
    }
}
