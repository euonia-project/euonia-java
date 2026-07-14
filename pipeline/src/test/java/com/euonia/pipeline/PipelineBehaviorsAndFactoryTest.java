package com.euonia.pipeline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.euonia.reflection.ServiceProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

    class SimpleServiceProvider implements ServiceProvider {
        @Override
        public <T> Optional<T> getService(Class<T> type) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> getService(Class<T> type, Class<?>... genericTypeArguments) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> getService(Class<T> type, String serviceName) {
            return Optional.empty();
        }

        @Override
        public <T> T getRequiredService(Class<T> type) {
            return createInstance(type);
        }

        @Override
        public <T> List<T> getServices(Class<T> type) {
            return List.of();
        }

        @Override
        public <T> List<T> getServices(Class<T> type, Class<?>... genericTypeArguments) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T createInstance(Class<T> type, Object... constructorArguments) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == constructorArguments.length) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    boolean matches = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (constructorArguments[i] != null
                            && !paramTypes[i].isAssignableFrom(constructorArguments[i].getClass())) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        try {
                            constructor.setAccessible(true);
                            return (T) constructor.newInstance(constructorArguments);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("Failed to create instance of " + type.getName(), e);
                        }
                    }
                }
            }
            throw new RuntimeException("No matching constructor found for " + type.getName());
        }
    }
}
