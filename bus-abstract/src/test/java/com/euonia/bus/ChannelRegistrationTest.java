package com.euonia.bus;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link ChannelRegistration}。
 */
@SuppressWarnings("unused")
@DisplayName("ChannelRegistration")
class ChannelRegistrationTest {

    private static ChannelHandler dummyHandler() {
        try {
            Method m = Object.class.getMethod("toString");
            return new ChannelHandler(Object.class, m, new Object());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should store message type")
        void shouldStoreMessageType() {
            var reg = new ChannelRegistration(String.class);

            assertThat(reg.getMessageType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should have empty handlers initially")
        void shouldHaveEmptyHandlersInitially() {
            var reg = new ChannelRegistration(Integer.class);

            assertThat(reg.getHandlers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("addHandler")
    class AddHandler {

        @Test
        @DisplayName("should add handler and return this for chaining")
        void shouldAddHandlerAndReturnThis() {
            var reg = new ChannelRegistration(String.class);
            var handler = dummyHandler();

            var result = reg.addHandler(handler);

            assertThat(result).isSameAs(reg);
            assertThat(reg.getHandlers()).containsExactly(handler);
        }

        @Test
        @DisplayName("should support multiple handlers")
        void shouldSupportMultipleHandlers() {
            var reg = new ChannelRegistration(String.class);
            var h1 = dummyHandler();
            var h2 = dummyHandler();

            reg.addHandler(h1).addHandler(h2);

            assertThat(reg.getHandlers()).containsExactly(h1, h2);
        }
    }

    @Nested
    @DisplayName("getHandlers")
    class GetHandlers {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            var reg = new ChannelRegistration(String.class);
            reg.addHandler(dummyHandler());

            List<ChannelHandler> handlers = reg.getHandlers();

            assertThatThrownBy(() -> handlers.add(dummyHandler()))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
