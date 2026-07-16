package com.euonia.bus.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.euonia.bus.MessageEnvelope;
import com.euonia.pipeline.Pipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.Bus;
import com.euonia.bus.options.SendOptions;

/**
 * 测试 {@link SendBuilder} 的链式构造和委托行为。
 */
@DisplayName("SendBuilder")
class SendBuilderTest {

    private static <T, R> SendBuilder<T, R> builderFor(Bus bus, T message, Class<R> responseType) {
        return new SendBuilder<>(bus, message, responseType);
    }

    private static Bus createBus(AtomicReference<Object> capturedMessage,
                                  AtomicReference<Class<?>> capturedResponseType,
                                  AtomicReference<Flow.Subscriber<?>> capturedCallback,
                                  AtomicReference<SendOptions> capturedOptions,
                                  AtomicReference<Consumer<?>> capturedBehavior) {
        return new Bus() {
            @Override
            public <T> CompletableFuture<Void> publishAsync(T message, com.euonia.bus.options.PublishOptions options,
                                                             Consumer<Pipeline<MessageEnvelope<T>, Void>> behavior) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback,
                                                             SendOptions sendOptions,
                                                             Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
                capturedMessage.set(message);
                capturedResponseType.set(responseType);
                capturedCallback.set(callback);
                capturedOptions.set(sendOptions);
                capturedBehavior.set(behavior);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType,
                                                          com.euonia.bus.options.CallOptions callOptions,
                                                          Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
                return CompletableFuture.completedFuture(null);
            }
        };
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create builder via Bus.send() without response type")
        void shouldCreateViaBusSendWithoutResponseType() {
            Bus bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = bus.send("command");
            assertThat(builder).isNotNull().isInstanceOf(SendBuilder.class);
        }

        @Test
        @DisplayName("should create builder via Bus.send() with response type")
        void shouldCreateViaBusSendWithResponseType() {
            Bus bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = bus.send("command", String.class);
            assertThat(builder).isNotNull().isInstanceOf(SendBuilder.class);
        }
    }

    @Nested
    @DisplayName("chain configuration")
    class ChainConfiguration {

        @Test
        @DisplayName("should set channel on options")
        void shouldSetChannel() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withChannel("commands").executeAsync();

            assertThat(capturedOptions.get().getChannel()).isEqualTo("commands");
        }

        @Test
        @DisplayName("should set callback subscriber")
        void shouldSetCallback() {
            var capturedCallback = new AtomicReference<Flow.Subscriber<?>>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedCallback,
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            Flow.Subscriber<String> subscriber = new Flow.Subscriber<>() {
                @Override public void onSubscribe(Flow.Subscription s) {}
                @Override public void onNext(String item) {}
                @Override public void onError(Throwable t) {}
                @Override public void onComplete() {}
            };
            builder.withCallback(subscriber).executeAsync();

            assertThat(capturedCallback.get()).isSameAs(subscriber);
        }

        @Test
        @DisplayName("should configure behavior callback")
        void shouldConfigureBehavior() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), capturedBehavior);
            var builder = builderFor(bus, "msg", String.class);

            Consumer<Pipeline<MessageEnvelope<String>, String>> behavior = pm -> {};
            builder.withBehavior(behavior).executeAsync();

            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }

        @Test
        @DisplayName("should configure metadata setter")
        void shouldConfigureMetadata() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withMetadata(m -> m.put("k", "v")).executeAsync();

            assertThat(capturedOptions.get().getMetadataSetter()).isNotNull();
        }

        @Test
        @DisplayName("should set custom message ID")
        void shouldSetMessageId() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withMessageId("send-id-456").executeAsync();

            assertThat(capturedOptions.get().getMessageId()).isEqualTo("send-id-456");
        }

        @Test
        @DisplayName("should set correlation ID")
        void shouldSetCorrelationId() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withCorrelationId("corr-001").executeAsync();

            assertThat(capturedOptions.get().getCorrelationId()).isEqualTo("corr-001");
        }

        @Test
        @DisplayName("should set priority")
        void shouldSetPriority() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withPriority(8).executeAsync();

            assertThat(capturedOptions.get().getPriority()).isEqualTo(8);
        }

        @Test
        @DisplayName("should set timeout")
        void shouldSetTimeout() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withTimeout(10_000L).executeAsync();

            assertThat(capturedOptions.get().getTimeout()).isEqualTo(10_000L);
        }

        @Test
        @DisplayName("should set delay")
        void shouldSetDelay() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.withDelay(300L).executeAsync();

            assertThat(capturedOptions.get().getDelay()).isEqualTo(300L);
        }

        @Test
        @DisplayName("should chain multiple options correctly")
        void shouldChainMultipleOptions() {
            var capturedMessage = new AtomicReference<>();
            var capturedResponseType = new AtomicReference<Class<?>>();
            var capturedCallback = new AtomicReference<Flow.Subscriber<?>>();
            var capturedOptions = new AtomicReference<SendOptions>();
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var bus = createBus(capturedMessage, capturedResponseType, capturedCallback, capturedOptions, capturedBehavior);
            var builder = builderFor(bus, "command", Integer.class);

            Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<>() {
                @Override public void onSubscribe(Flow.Subscription s) {}
                @Override public void onNext(Integer item) {}
                @Override public void onError(Throwable t) {}
                @Override public void onComplete() {}
            };
            Consumer<Pipeline<MessageEnvelope<String>, Integer>> behavior = pm -> {};

            builder.withChannel("cmds")
                   .withCallback(subscriber)
                   .withBehavior(behavior)
                   .withMessageId("mid-789")
                   .withCorrelationId("cid-456")
                   .withPriority(3)
                   .withTimeout(2000L)
                   .withDelay(100L)
                   .executeAsync();

            assertThat(capturedMessage.get()).isEqualTo("command");
            assertThat(capturedResponseType.get()).isEqualTo(Integer.class);
            assertThat(capturedCallback.get()).isSameAs(subscriber);
            assertThat(capturedOptions.get().getChannel()).isEqualTo("cmds");
            assertThat(capturedOptions.get().getMessageId()).isEqualTo("mid-789");
            assertThat(capturedOptions.get().getCorrelationId()).isEqualTo("cid-456");
            assertThat(capturedOptions.get().getPriority()).isEqualTo(3);
            assertThat(capturedOptions.get().getTimeout()).isEqualTo(2000L);
            assertThat(capturedOptions.get().getDelay()).isEqualTo(100L);
            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }
    }

    @Nested
    @DisplayName("runAsync")
    class RunAsync {

        @Test
        @DisplayName("should return completed future when bus completes")
        void shouldReturnCompletedFuture() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            var future = builder.executeAsync();

            assertThat(future).isCompleted();
        }

        @Test
        @DisplayName("should pass message type to bus.sendAsync")
        void shouldPassResponseTypeToBus() {
            var capturedResponseType = new AtomicReference<Class<?>>();
            var bus = createBus(new AtomicReference<>(), capturedResponseType, new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", Integer.class);

            builder.executeAsync();

            assertThat(capturedResponseType.get()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("should create SendOptions for each execution")
        void shouldCreateSendOptions() {
            var capturedOptions = new AtomicReference<SendOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.executeAsync();

            assertThat(capturedOptions.get()).isNotNull();
            assertThat(capturedOptions.get()).isInstanceOf(SendOptions.class);
        }

        @Test
        @DisplayName("should pass null callback when not configured")
        void shouldPassNullCallbackByDefault() {
            var capturedCallback = new AtomicReference<Flow.Subscriber<?>>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedCallback,
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            builder.executeAsync();

            assertThat(capturedCallback.get()).isNull();
        }

        @Test
        @DisplayName("should pass null behavior when not configured")
        void shouldPassNullBehaviorByDefault() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), capturedBehavior);
            var builder = builderFor(bus, "msg", String.class);

            builder.executeAsync();

            assertThat(capturedBehavior.get()).isNull();
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("should reject null channel")
        void shouldRejectNullChannel() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withChannel(null))
                .withMessageContaining("channel");
        }

        @Test
        @DisplayName("should reject null callback")
        void shouldRejectNullCallback() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withCallback(null))
                .withMessageContaining("callback");
        }

        @Test
        @DisplayName("should reject null behavior")
        void shouldRejectNullBehavior() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withBehavior(null))
                .withMessageContaining("behavior");
        }

        @Test
        @DisplayName("should reject null metadata setter")
        void shouldRejectNullMetadata() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withMetadata(null))
                .withMessageContaining("metadataSetter");
        }

        @Test
        @DisplayName("should reject null message ID")
        void shouldRejectNullMessageId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withMessageId(null))
                .withMessageContaining("messageId");
        }

        @Test
        @DisplayName("should reject empty message ID")
        void shouldRejectEmptyMessageId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withMessageId(""))
                .withMessageContaining("messageId");
        }

        @Test
        @DisplayName("should reject null correlation ID")
        void shouldRejectNullCorrelationId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withCorrelationId(null))
                .withMessageContaining("correlationId");
        }

        @Test
        @DisplayName("should reject empty correlation ID")
        void shouldRejectEmptyCorrelationId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "msg", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withCorrelationId(""))
                .withMessageContaining("correlationId");
        }
    }
}
