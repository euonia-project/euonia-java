package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link LambdaHandler}。
 */
@SuppressWarnings("unused")
@DisplayName("LambdaHandler")
class LambdaHandlerTest {

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("should invoke the wrapped function")
        void shouldInvokeWrappedFunction() {
            var handler = new LambdaHandler<String, Integer>((msg, ctx) -> msg.length());

            var result = handler.handle("hello", new MessageContextBase("hello"));

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("should forward context to function")
        void shouldForwardContext() {
            var handler = new LambdaHandler<String, String>((msg, ctx) -> {
                ctx.setMessageId("ctx-id");
                return ctx.getMessageId();
            });

            var ctx = new MessageContextBase("test");
            var result = handler.handle("test", ctx);

            assertThat(result).isEqualTo("ctx-id");
            assertThat(ctx.getMessageId()).isEqualTo("ctx-id");
        }

        @Test
        @DisplayName("should handle null return from function")
        void shouldHandleNullReturn() {
            var handler = new LambdaHandler<String, String>((msg, ctx) -> null);

            var result = handler.handle("anything", new MessageContextBase("anything"));

            assertThat(result).isNull();
        }
    }
}
