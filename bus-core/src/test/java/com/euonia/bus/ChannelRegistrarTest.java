package com.euonia.bus;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link ChannelRegistrar} 的通道注册、查询和验证逻辑。
 */
@DisplayName("ChannelRegistrar")
class ChannelRegistrarTest {

    // 每次测试后重置单例状态
    @AfterEach
    void tearDown() {
        // 通过反射重置单例，避免测试间状态泄漏
        try {
            var field = com.euonia.core.Singleton.class.getDeclaredField("instances");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var instances = (java.util.concurrent.ConcurrentMap<Class<?>, Object>) field.get(null);
            instances.remove(ChannelRegistrar.class);
        } catch (Exception ignored) {
            // 如果反射失败则忽略
        }
    }

    // ── 辅助 ─────────────────────────────────────────────────────

    static class TestMessage {}

    static class OtherMessage {}

    static ChannelHandler dummyHandler() {
        try {
            Method m = Object.class.getMethod("toString");
            return new ChannelHandler(Object.class, m, new Object());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // ── instance / 单例 ──────────────────────────────────────────

    @Nested
    @DisplayName("instance")
    class InstanceTests {

        @Test
        @DisplayName("should return same instance on multiple calls")
        void shouldReturnSameInstance() {
            var a = ChannelRegistrar.instance();
            var b = ChannelRegistrar.instance();

            assertThat(a).isSameAs(b);
        }
    }

    // ── getRegistration ──────────────────────────────────────────

    @Nested
    @DisplayName("getRegistration")
    class GetRegistrationTests {

        @Test
        @DisplayName("should return empty for unregistered channel")
        void shouldReturnEmptyForUnregisteredChannel() {
            var result = ChannelRegistrar.getRegistration("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return registration for registered channel")
        void shouldReturnRegistrationForRegisteredChannel() {
            ChannelRegistrar.instance().register("orders", TestMessage.class, dummyHandler());

            var result = ChannelRegistrar.getRegistration("orders");

            assertThat(result).isPresent();
            assertThat(result.get().getMessageType()).isEqualTo(TestMessage.class);
        }
    }

    // ── getRegistrations ─────────────────────────────────────────

    @Nested
    @DisplayName("getRegistrations")
    class GetRegistrationsTests {

        @Test
        @DisplayName("should return empty map when nothing registered")
        void shouldReturnEmptyMapWhenNothingRegistered() {
            var registrations = ChannelRegistrar.getRegistrations();

            assertThat(registrations).isEmpty();
        }

        @Test
        @DisplayName("should return unmodifiable map with registered channels")
        void shouldReturnUnmodifiableMapWithRegisteredChannels() {
            ChannelRegistrar.instance().register("ch1", TestMessage.class, dummyHandler());
            ChannelRegistrar.instance().register("ch2", OtherMessage.class, dummyHandler());

            var registrations = ChannelRegistrar.getRegistrations();

            assertThat(registrations).hasSize(2);
            assertThat(registrations).containsKeys("ch1", "ch2");
        }
    }

    // ── register(channel, messageType, handler) ──────────────────

    @Nested
    @DisplayName("register(String, Class, ChannelHandler)")
    class RegisterSingleHandlerTests {

        @Test
        @DisplayName("should register handler and add to channel")
        void shouldRegisterHandlerAndAddToChannel() {
            ChannelRegistrar.instance().register("test", TestMessage.class, dummyHandler());

            var reg = ChannelRegistrar.getRegistration("test").orElseThrow();
            assertThat(reg.getMessageType()).isEqualTo(TestMessage.class);
            assertThat(reg.getHandlers()).hasSize(1);
        }

        @Test
        @DisplayName("should add multiple handlers to same channel")
        void shouldAddMultipleHandlersToSameChannel() {
            var registrar = ChannelRegistrar.instance();
            registrar.register("shared", TestMessage.class, dummyHandler());
            registrar.register("shared", TestMessage.class, dummyHandler());

            var reg = ChannelRegistrar.getRegistration("shared").orElseThrow();
            assertThat(reg.getHandlers()).hasSize(2);
        }

        @Test
        @DisplayName("should throw when registering with different message type on same channel")
        void shouldThrowWhenDifferentMessageTypeOnSameChannel() {
            var registrar = ChannelRegistrar.instance();
            registrar.register("shared", TestMessage.class, dummyHandler());

            assertThatThrownBy(() -> registrar.register("shared", OtherMessage.class, dummyHandler()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ChannelRegistrarTest$OtherMessage");
        }

        @Test
        @DisplayName("should reject null messageType")
        void shouldRejectNullMessageType() {
            assertThatThrownBy(() -> ChannelRegistrar.instance().register("ch", null, dummyHandler()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("messageType");
        }

        @Test
        @DisplayName("should reject null handler")
        void shouldRejectNullHandler() {
            assertThatThrownBy(() -> ChannelRegistrar.instance().register("ch", TestMessage.class, (ChannelHandler) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("handler");
        }

        @Test
        @DisplayName("should return this for chaining")
        void shouldReturnThisForChaining() {
            var result = ChannelRegistrar.instance().register("ch", TestMessage.class, dummyHandler());

            assertThat(result).isSameAs(ChannelRegistrar.instance());
        }
    }

    // ── register(channel, messageType, List<ChannelHandler>) ─────

    @Nested
    @DisplayName("register(String, Class, List<ChannelHandler>)")
    class RegisterListHandlerTests {

        @Test
        @DisplayName("should register multiple handlers at once")
        void shouldRegisterMultipleHandlersAtOnce() {
            var handlers = List.of(dummyHandler(), dummyHandler(), dummyHandler());
            ChannelRegistrar.instance().register("batch", TestMessage.class, handlers);

            var reg = ChannelRegistrar.getRegistration("batch").orElseThrow();
            assertThat(reg.getHandlers()).hasSize(3);
        }

        @Test
        @DisplayName("should reject empty handler list")
        void shouldRejectEmptyHandlerList() {
            assertThatThrownBy(() -> ChannelRegistrar.instance().register("ch", TestMessage.class, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject null handler list")
        void shouldRejectNullHandlerList() {
            assertThatThrownBy(() -> ChannelRegistrar.instance().register("ch", TestMessage.class, (List<ChannelHandler>) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── register(channel, messageType, BiFunction) ───────────────

    @Nested
    @DisplayName("register(String, Class, BiFunction)")
    class RegisterLambdaHandlerTests {

        @Test
        @DisplayName("should register lambda handler")
        void shouldRegisterLambdaHandler() {
            ChannelRegistrar.instance().register("lambda", String.class, (msg, ctx) -> msg.toUpperCase());

            var reg = ChannelRegistrar.getRegistration("lambda").orElseThrow();
            assertThat(reg.getHandlers()).hasSize(1);
            assertThat(reg.getHandlers().get(0).handlerType()).isEqualTo(LambdaHandler.class);
        }
    }
}
