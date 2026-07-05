package com.euonia.bus.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

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
import com.euonia.bus.options.CallOptions;

/**
 * 测试 {@link CallBuilder} 的链式构造和委托行为。
 */
@DisplayName("CallBuilder")
class CallBuilderTest {

    private static <T, R> CallBuilder<T, R> builderFor(Bus bus, T request, Class<R> responseType) {
        return new CallBuilder<>(bus, request, responseType);
    }

    private static Bus createBus(AtomicReference<Object> capturedRequest,
                                  AtomicReference<Class<?>> capturedResponseType,
                                  AtomicReference<CallOptions> capturedOptions,
                                  AtomicReference<Consumer<?>> capturedBehavior,
                                  Object responseValue) {
        return new Bus() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> CompletableFuture<Void> publishAsync(T message, com.euonia.bus.options.PublishOptions options,
                                                             Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType,
                                                             java.util.concurrent.Flow.Subscriber<R> callback,
                                                             com.euonia.bus.options.SendOptions sendOptions,
                                                             Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
                return CompletableFuture.completedFuture(null);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, CallOptions callOptions,
                                                          Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
                capturedRequest.set(request);
                capturedResponseType.set(responseType);
                capturedOptions.set(callOptions);
                capturedBehavior.set((Consumer<?>) (Object) behavior);
                return CompletableFuture.completedFuture((R) responseValue);
            }
        };
    }

    private static Bus createBus(AtomicReference<Object> capturedRequest,
                                  AtomicReference<Class<?>> capturedResponseType,
                                  AtomicReference<CallOptions> capturedOptions,
                                  AtomicReference<Consumer<?>> capturedBehavior) {
        return createBus(capturedRequest, capturedResponseType, capturedOptions, capturedBehavior, null);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create builder via Bus.call()")
        void shouldCreateViaBusCall() {
            Bus bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = bus.call("request", String.class);
            assertThat(builder).isNotNull().isInstanceOf(CallBuilder.class);
        }
    }

    @Nested
    @DisplayName("chain configuration")
    class ChainConfiguration {

        @Test
        @DisplayName("should set channel on options")
        void shouldSetChannel() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withChannel("queries").runAsync();

            assertThat(capturedOptions.get().getChannel()).isEqualTo("queries");
        }

        @Test
        @DisplayName("should configure behavior callback")
        void shouldConfigureBehavior() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);
            var builder = builderFor(bus, "req", String.class);

            Consumer<PipelineMessage<RoutedMessage<String>, String>> behavior = pm -> {};
            builder.withBehavior(behavior).runAsync();

            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }

        @Test
        @DisplayName("should configure metadata setter")
        void shouldConfigureMetadata() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withMetadata(m -> m.put("k", "v")).runAsync();

            assertThat(capturedOptions.get().getMetadataSetter()).isNotNull();
        }

        @Test
        @DisplayName("should set custom message ID")
        void shouldSetMessageId() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withMessageId("call-id-001").runAsync();

            assertThat(capturedOptions.get().getMessageId()).isEqualTo("call-id-001");
        }

        @Test
        @DisplayName("should set correlation ID")
        void shouldSetCorrelationId() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withCorrelationId("corr-002").runAsync();

            assertThat(capturedOptions.get().getCorrelationId()).isEqualTo("corr-002");
        }

        @Test
        @DisplayName("should set priority")
        void shouldSetPriority() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withPriority(7).runAsync();

            assertThat(capturedOptions.get().getPriority()).isEqualTo(7);
        }

        @Test
        @DisplayName("should set timeout")
        void shouldSetTimeout() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withTimeout(8000L).runAsync();

            assertThat(capturedOptions.get().getTimeout()).isEqualTo(8000L);
        }

        @Test
        @DisplayName("should set delay")
        void shouldSetDelay() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.withDelay(250L).runAsync();

            assertThat(capturedOptions.get().getDelay()).isEqualTo(250L);
        }

        @Test
        @DisplayName("should chain multiple options correctly")
        void shouldChainMultipleOptions() {
            var capturedRequest = new AtomicReference<Object>();
            var capturedResponseType = new AtomicReference<Class<?>>();
            var capturedOptions = new AtomicReference<CallOptions>();
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var bus = createBus(capturedRequest, capturedResponseType, capturedOptions, capturedBehavior);
            var builder = builderFor(bus, "query-request", Integer.class);

            Consumer<PipelineMessage<RoutedMessage<String>, Integer>> behavior = pm -> {};

            builder.withChannel("search")
                   .withBehavior(behavior)
                   .withMetadata(m -> m.put("auth", "token"))
                   .withMessageId("mid-100")
                   .withCorrelationId("cid-200")
                   .withPriority(2)
                   .withTimeout(6000L)
                   .withDelay(50L)
                   .runAsync();

            assertThat(capturedRequest.get()).isEqualTo("query-request");
            assertThat(capturedResponseType.get()).isEqualTo(Integer.class);
            assertThat(capturedOptions.get().getChannel()).isEqualTo("search");
            assertThat(capturedOptions.get().getMessageId()).isEqualTo("mid-100");
            assertThat(capturedOptions.get().getCorrelationId()).isEqualTo("cid-200");
            assertThat(capturedOptions.get().getPriority()).isEqualTo(2);
            assertThat(capturedOptions.get().getTimeout()).isEqualTo(6000L);
            assertThat(capturedOptions.get().getDelay()).isEqualTo(50L);
            assertThat(capturedBehavior.get()).isSameAs(behavior);
        }
    }

    @Nested
    @DisplayName("runAsync")
    class RunAsync {

        @Test
        @DisplayName("should return completed future with response value")
        void shouldReturnResponseValue() throws Exception {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>(), "response");
            var builder = builderFor(bus, "req", String.class);

            var future = builder.runAsync();

            assertThat(future).isCompletedWithValue("response");
        }

        @Test
        @DisplayName("should pass request type to bus.callAsync")
        void shouldPassResponseTypeToBus() {
            var capturedResponseType = new AtomicReference<Class<?>>();
            var bus = createBus(new AtomicReference<>(), capturedResponseType, new AtomicReference<>(), new AtomicReference<>());
            var builder = builderFor(bus, "req", Long.class);

            builder.runAsync();

            assertThat(capturedResponseType.get()).isEqualTo(Long.class);
        }

        @Test
        @DisplayName("should create CallOptions for each execution")
        void shouldCreateCallOptions() {
            var capturedOptions = new AtomicReference<CallOptions>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), capturedOptions, new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            builder.runAsync();

            assertThat(capturedOptions.get()).isNotNull();
            assertThat(capturedOptions.get()).isInstanceOf(CallOptions.class);
        }

        @Test
        @DisplayName("should pass null behavior when not configured")
        void shouldPassNullBehaviorByDefault() {
            var capturedBehavior = new AtomicReference<Consumer<?>>();
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), capturedBehavior);
            var builder = builderFor(bus, "req", String.class);

            builder.runAsync();

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
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withChannel(null))
                .withMessageContaining("channel");
        }

        @Test
        @DisplayName("should reject null behavior")
        void shouldRejectNullBehavior() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withBehavior(null))
                .withMessageContaining("behavior");
        }

        @Test
        @DisplayName("should reject null metadata setter")
        void shouldRejectNullMetadata() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withMetadata(null))
                .withMessageContaining("metadataSetter");
        }

        @Test
        @DisplayName("should reject null message ID")
        void shouldRejectNullMessageId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withMessageId(null))
                .withMessageContaining("messageId");
        }

        @Test
        @DisplayName("should reject empty message ID")
        void shouldRejectEmptyMessageId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withMessageId(""))
                .withMessageContaining("messageId");
        }

        @Test
        @DisplayName("should reject null correlation ID")
        void shouldRejectNullCorrelationId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withCorrelationId(null))
                .withMessageContaining("correlationId");
        }

        @Test
        @DisplayName("should reject empty correlation ID")
        void shouldRejectEmptyCorrelationId() {
            var bus = createBus(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                new AtomicReference<>());
            var builder = builderFor(bus, "req", String.class);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.withCorrelationId(""))
                .withMessageContaining("correlationId");
        }
    }
}
