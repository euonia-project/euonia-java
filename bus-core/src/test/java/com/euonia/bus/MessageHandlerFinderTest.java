package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import com.euonia.bus.annotation.Channel;
import com.euonia.bus.annotation.Subscribe;
import com.euonia.bus.message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageHandlerFinder} 按类型解析处理器的功能。
 */
@SuppressWarnings("unused")
@DisplayName("MessageHandlerFinder")
class MessageHandlerFinderTest {

    // ── 辅助：收集 Delegate 调用的结果 ────────────────────────────

    /**
     * 记录 delegate.next() 调用的收集器。
     */
    record CallRecord(String channel, Class<?> messageType, ChannelHandler handler) {
    }

    static List<CallRecord> collectCalls(Class<?>... handlerTypes) {
        List<CallRecord> records = new ArrayList<>();
        MessageHandlerFinder.find((channel, messageType, handler) ->
                                      records.add(new CallRecord(channel, messageType, handler)), handlerTypes);
        return records;
    }

    // ── 测试用消息类 ─────────────────────────────────────────────

    @Channel("type-channel")
    static class AnnotatedMsg implements Message {
    }

    static class OrderCmd implements Message {
    }

    static class SimpleEvent implements Message {
    }

    /**
     * 未实现 Message 接口的普通类，用于测试通道名回退逻辑。
     */
    static class NonMessagePayload {
    }

    // ── 测试用处理器类 ───────────────────────────────────────────

    /**
     * 基础 @Subscribe 处理器 —— 指定通道名。
     */
    static class SubscribeHandler {
        int callCount;

        @Subscribe("test-channel")
        void handle(String message, MessageContext ctx) {
            callCount++;
        }
    }

    /**
     * {@code @Subscribe} 未指定通道名，依赖参数或类型上的 {@code @Channel}。
     */
    static class SubWithChannelHandler {
        @Subscribe("orders")
        void onOrder(OrderCmd cmd) {
        }
    }

    /**
     * {@code @Subscribe} 未指定通道名，通过参数 {@code @Channel} 指定。
     */
    static class SubWithParameterChannelHandler {
        @Subscribe
        void onOrder(@Channel("param-channel") OrderCmd cmd) {
        }
    }

    /**
     * {@code @Subscribe} 未指定通道名，通过参数类型 {@code @Channel} 指定。
     */
    static class SubWithTypeChannelHandler {
        @Subscribe
        void onOrder(AnnotatedMsg cmd) {
        }
    }

    /**
     * {@code @Subscribe} 未指定通道名，参数实现 {@code Message}，回退到类名。
     */
    static class SubWithMessageFallbackHandler {
        @Subscribe
        void onOrder(OrderCmd cmd) {
        }
    }

    /**
     * {@code @Subscribe} 未指定通道名，参数不是 {@code Message} 且无 {@code @Channel} —— 应抛异常。
     */
    static class SubscribeInvalidPayloadHandler {
        @Subscribe
        void onEvent(NonMessagePayload payload) {
        }
    }

    /**
     * {@code @Subscribe} 方法无参数 —— 应抛异常。
     */
    static class SubscribeNoParamHandler {
        @Subscribe
        void handle() {
        }
    }

    /**
     * {@code @Subscribe} 方法第二个参数不是 {@code MessageContext} —— 应抛异常。
     */
    static class SubscribeInvalidSecondParamHandler {
        @Subscribe
        void handle(String msg, String invalid) {
        }
    }

    /**
     * {@code Handler} 接口实现。
     */
    static class ImplementsHandler implements Handler<OrderCmd, String> {
        @Override
        public String handle(OrderCmd message, MessageContext context) {
            return "ok";
        }
    }

    /**
     * 同时有 {@code Handler} 接口和 {@code @Subscribe} 注解的类 —— {@code Handler} 优先，{@code @Subscribe} 不会重复注册。
     */
    static class MixedHandler implements Handler<OrderCmd, String> {
        @Override
        public String handle(OrderCmd message, MessageContext context) {
            return "ok";
        }

        @Subscribe("subscribe-channel")
        void onSubscribe(SimpleEvent event) {
        }
    }

    /**
     * 接口 —— 应被跳过。
     */
    interface HandlerInterface extends Handler<OrderCmd, String> {
    }

    /**
     * 枚举 —— 应被跳过。
     */
    enum HandlerEnum {
        INSTANCE
    }

    /**
     * 记录 —— 应被跳过 (Java 14+)。
     */
    record HandlerRecord(String name) {
    }

    // ── find(Delegate, Class<?>...) ──────────────────────────────

    @Nested
    @DisplayName("find(Delegate, Class<?>...) - handler type resolution")
    class FindByHandlerType {

        @Test
        @DisplayName("Given @Subscribe with channel value then register with specified channel")
        void givenSubscribeWithChannelValueThenRegisterWithSpecifiedChannel() {
            List<CallRecord> records = collectCalls(SubscribeHandler.class);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).channel()).isEqualTo("test-channel");
            assertThat(records.get(0).messageType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Given @Subscribe without channel then resolve from parameter @Channel")
        void givenSubscribeWithoutChannelThenResolveFromParameterChannel() {
            List<CallRecord> records = collectCalls(SubWithParameterChannelHandler.class);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).channel()).isEqualTo("param-channel");
        }

        @Test
        @DisplayName("Given @Subscribe without channel then resolve from type @Channel")
        void givenSubscribeWithoutChannelThenResolveFromTypeChannel() {
            List<CallRecord> records = collectCalls(SubWithTypeChannelHandler.class);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).channel()).isEqualTo("type-channel");
        }

        @Test
        @DisplayName("Given @Subscribe without channel on Message type then fallback to type name")
        void givenSubscribeWithoutChannelOnMessageTypeThenFallbackToTypeName() {
            List<CallRecord> records = collectCalls(SubWithMessageFallbackHandler.class);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).channel()).isEqualTo(OrderCmd.class.getName());
        }

        @Test
        @DisplayName("Given @Subscribe without channel on non-Message type then throw IllegalStateException")
        void givenSubscribeWithoutChannelOnNonMessageTypeThenThrowIllegalStateException() {
            assertThatThrownBy(() -> collectCalls(SubscribeInvalidPayloadHandler.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Message");
        }

        @Test
        @DisplayName("Given @Subscribe method with zero parameters then throw IllegalStateException")
        void givenSubscribeMethodWithZeroParametersThenThrowIllegalStateException() {
            assertThatThrownBy(() -> collectCalls(SubscribeNoParamHandler.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MessageHandlerFinderTest$SubscribeNoParamHandler.handle");
        }

        @Test
        @DisplayName("Given @Subscribe method with invalid second parameter then throw IllegalStateException")
        void givenSubscribeMethodWithInvalidSecondParameterThenThrowIllegalStateException() {
            assertThatThrownBy(() -> collectCalls(SubscribeInvalidSecondParamHandler.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MessageContext");
        }

        @Test
        @DisplayName("Given Handler interface implementation then register via generic type")
        void givenHandlerInterfaceImplementationThenRegisterViaGenericType() {
            List<CallRecord> records = collectCalls(ImplementsHandler.class);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).channel()).isEqualTo(OrderCmd.class.getName());
            assertThat(records.get(0).messageType()).isEqualTo(OrderCmd.class);
        }

        @Test
        @DisplayName("Given mixed Handler and @Subscribe then register both without duplicate")
        void givenMixedHandlerAndSubscribeThenRegisterBothWithoutDuplicate() {
            List<CallRecord> records = collectCalls(MixedHandler.class);

            assertThat(records).hasSize(2);
            // Handler 接口的注册（通道名为消息类型全限定名）
            assertThat(records).anyMatch(r ->
                                             r.channel().equals(OrderCmd.class.getName())
                                                 && r.messageType().equals(OrderCmd.class));
            // @Subscribe 方法的注册
            assertThat(records).anyMatch(r ->
                                             r.channel().equals("subscribe-channel")
                                                 && r.messageType().equals(SimpleEvent.class));
        }

        @Test
        @DisplayName("Given interface handler type then skip and register nothing")
        void givenInterfaceHandlerTypeThenSkipAndRegisterNothing() {
            List<CallRecord> records = collectCalls(HandlerInterface.class);

            assertThat(records).isEmpty();
        }

        @Test
        @DisplayName("Given enum handler type then skip and register nothing")
        void givenEnumHandlerTypeThenSkipAndRegisterNothing() {
            List<CallRecord> records = collectCalls(HandlerEnum.class);

            assertThat(records).isEmpty();
        }

        @Test
        @DisplayName("Given record handler type then skip and register nothing")
        void givenRecordHandlerTypeThenSkipAndRegisterNothing() {
            List<CallRecord> records = collectCalls(HandlerRecord.class);

            assertThat(records).isEmpty();
        }

        @Test
        @DisplayName("Given multiple handler types then register all valid handlers")
        void givenMultipleHandlerTypesThenRegisterAllValidHandlers() {
            List<CallRecord> records = collectCalls(
                SubscribeHandler.class, ImplementsHandler.class);

            assertThat(records).hasSize(2);
        }

        @Test
        @DisplayName("Given handler with @Subscribe specifying channel then channel annotation takes priority")
        void givenHandlerWithSubscribeSpecifyingChannelThenChannelAnnotationTakesPriority() {
            List<CallRecord> records = collectCalls(SubWithChannelHandler.class);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).channel()).isEqualTo("orders");
        }
    }
}

