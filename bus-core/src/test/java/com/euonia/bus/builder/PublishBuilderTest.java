package com.euonia.bus.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.Bus;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.PipelineMessage;
import com.euonia.bus.RoutedMessage;
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
            public <T> CompletableFuture<Void> publishAsync(T msg, PublishOptions opts, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
                capturedMessage.set((String) msg);
                capturedOptions.set(opts);
                capturedBehavior.set((Consumer<?>) (Object) behavior);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, java.util.concurrent.Flow.Subscriber<R> callback,
                                                             com.euonia.bus.options.SendOptions sendOptions,
                                                             Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, com.euonia.bus.options.CallOptions callOptions,
                                                          Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
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
                public <T> CompletableFuture<Void> publishAsync(T message, PublishOptions options, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, java.util.concurrent.Flow.Subscriber<R> callback,
                                                                 com.euonia.bus.options.SendOptions sendOptions,
                                                                 Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, com.euonia.bus.options.CallOptions callOptions,
                                                              Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
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

            builder.withChannel("orders").runAsync();

            assertThat(capturedOptions.get().getChannel()).isEqualTo("orders");
        }

        @Test
        @DisplayName("should configure behavior callback")
        void shouldConfigureBehavior() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);

            Consumer<PipelineMessage<RoutedMessage<String>, Void>> behavior = pm -> {};
            builder.withBehavior(behavior).runAsync();

            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }

        @Test
        @DisplayName("should configure metadata setter")
        void shouldConfigureMetadata() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withMetadata(m -> m.put("key", "value")).runAsync();

            assertThat(capturedOptions.get().getMetadataSetter()).isNotNull();
        }

        @Test
        @DisplayName("should set custom message ID")
        void shouldSetMessageId() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withMessageId("custom-id-123").runAsync();

            assertThat(capturedOptions.get().getMessageId()).isEqualTo("custom-id-123");
        }

        @Test
        @DisplayName("should set priority")
        void shouldSetPriority() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withPriority(5).runAsync();

            assertThat(capturedOptions.get().getPriority()).isEqualTo(5);
        }

        @Test
        @DisplayName("should set timeout")
        void shouldSetTimeout() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withTimeout(3000L).runAsync();

            assertThat(capturedOptions.get().getTimeout()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("should set delay")
        void shouldSetDelay() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.withDelay(500L).runAsync();

            assertThat(capturedOptions.get().getDelay()).isEqualTo(500L);
        }

        @Test
        @DisplayName("should chain multiple options correctly")
        void shouldChainMultipleOptions() {
            var capturedMessage = new AtomicReference<String>();
            var capturedOptions = new AtomicReference<PublishOptions>();
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("event", capturedMessage, capturedOptions, capturedBehavior);

            Consumer<PipelineMessage<RoutedMessage<String>, Void>> behavior = pm -> {};
            builder.withChannel("notifications")
                   .withMessageId("msg-001")
                   .withPriority(10)
                   .withTimeout(5000L)
                   .withDelay(200L)
                   .withBehavior(behavior)
                   .runAsync();

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

            var future = builder.runAsync();

            assertThat(future).isCompleted();
        }

        @Test
        @DisplayName("should pass message to bus.publishAsync")
        void shouldPassMessageToBus() {
            var capturedMessage = new AtomicReference<String>();
            var builder = builderFor("test-message", capturedMessage, new AtomicReference<>(), new AtomicReference<>());

            builder.runAsync();

            assertThat(capturedMessage.get()).isEqualTo("test-message");
        }

        @Test
        @DisplayName("should pass PublishOptions to bus.publishAsync")
        void shouldPassPublishOptions() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            builder.runAsync();

            assertThat(capturedOptions.get()).isNotNull();
            assertThat(capturedOptions.get()).isInstanceOf(PublishOptions.class);
        }

        @Test
        @DisplayName("should pass null behavior when not configured")
        void shouldPassNullBehaviorByDefault() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);

            builder.runAsync();

            assertThat(capturedBehavior.get()).isNull();
        }
    }

    @Nested
    @DisplayName("null safety")
    class NullSafety {

        @Test
        @DisplayName("should accept null channel")
        void shouldAcceptNullChannel() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            assertThatNoException().isThrownBy(() -> {
                builder.withChannel(null);
                builder.runAsync();
            });
            assertThat(capturedOptions.get().getChannel()).isNull();
        }

        @Test
        @DisplayName("should accept null behavior")
        void shouldAcceptNullBehavior() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var builder = builderFor("msg", new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);

            builder.withBehavior(null).runAsync();

            assertThat(capturedBehavior.get()).isNull();
        }

        @Test
        @DisplayName("should accept null metadata setter")
        void shouldAcceptNullMetadata() {
            var capturedOptions = new AtomicReference<PublishOptions>();
            var builder = builderFor("msg", new AtomicReference<>(), capturedOptions, new AtomicReference<>());

            assertThatNoException().isThrownBy(() -> {
                builder.withMetadata(null).runAsync();
            });
        }
    }
}
