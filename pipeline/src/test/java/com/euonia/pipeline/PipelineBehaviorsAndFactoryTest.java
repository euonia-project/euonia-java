package com.euonia.pipeline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.reflection.SimpleServiceProvider;

/**
 * 测试 {@link PipelineBehaviors} 注解和 {@link PipelineFactory}。
 */
@SuppressWarnings("unused")
@DisplayName("PipelineBehaviors / PipelineFactory")
class PipelineBehaviorsAndFactoryTest {

    @Nested
    @DisplayName("PipelineBehaviors annotation")
    class AnnotationTests {

        @Test
        @DisplayName("should have runtime retention")
        void shouldHaveRuntimeRetention() {
            var retention = PipelineBehaviors.class.getAnnotation(Retention.class);
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target types")
        void shouldTargetTypes() {
            var target = PipelineBehaviors.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target.value()).contains(java.lang.annotation.ElementType.TYPE);
        }
    }

    @Nested
    @DisplayName("PipelineFactory")
    class FactoryTests {

        @Test
        @DisplayName("should create pipeline via DefaultPipelineFactory")
        void shouldCreatePipelineViaDefaultPipelineFactory() {
            var provider = new SimpleServiceProvider();
            var factory = new DefaultPipelineFactory(provider);

            var pipeline = factory.create();

            assertThat(pipeline).isNotNull();
            assertThat(pipeline).isInstanceOf(Pipeline.class);
        }
    }
}
