package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;

import com.euonia.bus.contract.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.annotation.Subscribe;

/**
 * 测试 {@link MessageHandlerFinder} 按类型解析处理器的功能。
 */
@SuppressWarnings("unused")
@DisplayName("MessageHandlerFinder")
class MessageHandlerFinderTest {

    // 测试用处理器类
    static class SubscribeHandler {
        int callCount;

        @Subscribe("test-channel")
        void handle(String message, MessageContext ctx) {
            callCount++;
        }
    }

    static class SubWithChannelHandler {
        @Subscribe("orders")
        void onOrder(OrderCmd cmd) {
        }
    }

    static class ImplementsHandler implements Handler<OrderCmd, String> {
        @Override
        public String handle(OrderCmd message, MessageContext context) {
            return "ok";
        }
    }

    // 测试用消息类
    static class OrderCmd implements Message {
    }

    static class SimpleEvent implements Message {
    }

    @Nested
    @DisplayName("class-based find")
    class ClassBasedFind {

        @Test
        @DisplayName("should find Subscribe-annotated methods with explicit channel")
        void shouldFindSubscribeMethodWithExplicitChannel() {
            var registrations = MessageHandlerFinder.find(SubWithChannelHandler.class);

            assertThat(registrations).hasSize(1);
            var reg = registrations.get(0);
            assertThat(reg.channel()).isEqualTo("orders");
            assertThat(reg.handlerType()).isEqualTo(SubWithChannelHandler.class);
        }

        @Test
        @DisplayName("should find Subscribe-annotated methods with context parameter")
        void shouldFindSubscribeMethodWithContext() {
            var registrations = MessageHandlerFinder.find(SubscribeHandler.class);

            assertThat(registrations).hasSize(1);
            assertThat(registrations.get(0).channel()).isEqualTo("test-channel");
        }

        @Test
        @DisplayName("should find Handler<M,R> implementations")
        void shouldFindHandlerImplementation() {
            var registrations = MessageHandlerFinder.find(ImplementsHandler.class);

            assertThat(registrations).hasSize(1);
            var reg = registrations.get(0);
            assertThat(reg.handlerType()).isEqualTo(ImplementsHandler.class);
            assertThat(reg.messageType()).isEqualTo(OrderCmd.class);
        }

        @Test
        @DisplayName("should skip interfaces and records")
        void shouldSkipInterfaces() {
            var registrations = MessageHandlerFinder.find(java.io.Serializable.class);

            assertThat(registrations).isEmpty();
        }
    }

    @Nested
    @DisplayName("array-based find")
    class ArrayBasedFind {

        @Test
        @DisplayName("should find handlers from array of classes")
        void shouldFindFromArray() {
            var registrations = MessageHandlerFinder.find(
                SubscribeHandler.class, ImplementsHandler.class);

            assertThat(registrations).hasSize(2);
        }
    }
}
