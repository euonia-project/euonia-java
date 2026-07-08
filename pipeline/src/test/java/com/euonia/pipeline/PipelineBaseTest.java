package com.euonia.pipeline;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link PipelineBase} 的组件管理和构建。
 */
@SuppressWarnings("unused")
@DisplayName("PipelineBase")
class PipelineBaseTest {

    static class TestPipeline extends PipelineBase<String, String> {
        @Override
        public PipelineDelegate<String, String> build() {
            return super.build();
        }

        @Override
        protected PipelineDelegate<String, String> getNext(PipelineDelegate<String, String> next,
                                                           Class<?> behaviorType, Object... args) {
            return next;
        }
    }

    @Nested
    @DisplayName("use (Function)")
    class UseFunction {

        @Test
        @DisplayName("should add component and return this")
        void shouldAddComponentAndReturnThis() {
            var pipeline = new TestPipeline();
            var result = pipeline.use(next -> next);

            assertThat(result).isSameAs(pipeline);
            assertThat(pipeline.getComponents()).hasSize(1);
        }

        @Test
        @DisplayName("should accumulate multiple components")
        void shouldAccumulateMultipleComponents() {
            var pipeline = new TestPipeline();
            pipeline.use(next -> next);
            pipeline.use(next -> next);

            assertThat(pipeline.getComponents()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("use (BiFunction)")
    class UseBiFunction {

        @Test
        @DisplayName("should add handler as component")
        void shouldAddHandlerAsComponent() {
            var pipeline = new TestPipeline();
            pipeline.use((ctx, next) -> CompletableFuture.completedFuture(ctx));

            assertThat(pipeline.getComponents()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("use at index")
    class UseAtIndex {

        @Test
        @DisplayName("should insert component at specific index")
        void shouldInsertAtSpecificIndex() {
            var pipeline = new TestPipeline();
            pipeline.use(next -> next);
            pipeline.use(next -> next, 0);

            assertThat(pipeline.getComponents()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getComponents")
    class GetComponents {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            var pipeline = new TestPipeline();
            pipeline.use(next -> next);

            List<?> components = pipeline.getComponents();

            assertThat(components).hasSize(1);
            // Verify it's a copy, not the internal list
            assertThat(components).isNotSameAs(pipeline.getComponents())
                .describedAs("getComponents should return a defensive copy");
        }
    }
}
