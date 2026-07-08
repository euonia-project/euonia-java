package com.euonia.pipeline;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link PipelineDelegate} 和 {@link PipelineBehavior}。
 */
@SuppressWarnings("unused")
@DisplayName("PipelineDelegate / PipelineBehavior")
class PipelineDelegateAndBehaviorTest {

    @Nested
    @DisplayName("PipelineDelegate")
    class DelegateTests {

        @Test
        @DisplayName("should invoke and return CompletionStage")
        void shouldInvokeAndReturnCompletionStage() throws Exception {
            PipelineDelegate<String, String> delegate = ctx -> CompletableFuture.completedFuture("result: " + ctx);

            var result = delegate.invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("result: input");
        }

        @Test
        @DisplayName("should handle null context")
        void shouldHandleNullContext() throws Exception {
            PipelineDelegate<Void, String> delegate = ctx -> CompletableFuture.completedFuture("done");

            var result = delegate.invoke(null).toCompletableFuture().get();

            assertThat(result).isEqualTo("done");
        }

        @Test
        @DisplayName("should support exception propagation")
        void shouldSupportExceptionPropagation() {
            PipelineDelegate<String, String> failing = ctx ->
                CompletableFuture.failedFuture(new RuntimeException("test error"));

            var future = failing.invoke("test").toCompletableFuture();
            try {
                future.get();
            } catch (Exception e) {
                assertThat(e.getCause()).hasMessage("test error");
            }
        }
    }

    @Nested
    @DisplayName("PipelineBehavior")
    class BehaviorTests {

        @Test
        @DisplayName("should call next delegate in chain")
        void shouldCallNextDelegate() throws Exception {
            PipelineBehavior<String, String> behavior = (ctx, next) -> {
                ctx = "modified: " + ctx;
                return next.invoke(ctx);
            };
            PipelineDelegate<String, String> terminal = ctx -> CompletableFuture.completedFuture(ctx);

            var result = behavior.handleAsync("input", terminal).toCompletableFuture().get();

            assertThat(result).isEqualTo("modified: input");
        }

        @Test
        @DisplayName("should allow behavior to short-circuit chain")
        void shouldAllowShortCircuit() throws Exception {
            AtomicReference<String> called = new AtomicReference<>();
            PipelineBehavior<String, String> shortCircuit = (ctx, next) ->
                CompletableFuture.completedFuture("short-circuited");
            PipelineDelegate<String, String> next = ctx -> {
                called.set("should not be called");
                return CompletableFuture.completedFuture("from next");
            };

            var result = shortCircuit.handleAsync("input", next).toCompletableFuture().get();

            assertThat(result).isEqualTo("short-circuited");
            assertThat(called.get()).isNull();
        }
    }
}
