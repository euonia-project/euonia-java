package com.euonia.pipeline;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.euonia.reflection.ServiceProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

//import com.euonia.reflection.SimpleServiceProvider;

@SuppressWarnings("unused")
@DisplayName("DefaultPipelineProvider")
class DefaultPipelineProviderTest {

    @Nested
    @DisplayName("PipelineBehavior integration")
    class PipelineBehaviorTests {

        @Test
        @DisplayName("should call handleAsync when behavior implements PipelineBehavior")
        void shouldCallHandleAsyncWhenBehaviorImplementsPipelineBehavior() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(CustomBehavior.class);

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("handleAsync: input");
        }

        @Test
        @DisplayName("should forward to next delegate when behavior calls next.invoke")
        void shouldForwardToNextDelegateWhenBehaviorCallsNextInvoke() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(ForwardingBehavior.class);
            pipeline.use((ctx, next) -> CompletableFuture.completedFuture(ctx));

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("forwarded: input");
        }

        static class CustomBehavior implements PipelineBehavior<String, String> {
            @SuppressWarnings("unused")
            public CustomBehavior() {
            }

            @Override
            public CompletionStage<String> handleAsync(String context, PipelineDelegate<String, String> next) {
                return CompletableFuture.completedFuture("handleAsync: " + context);
            }
        }

        static class ForwardingBehavior implements PipelineBehavior<String, String> {
            @SuppressWarnings("unused")
            public ForwardingBehavior() {
            }

            @Override
            public CompletionStage<String> handleAsync(String context, PipelineDelegate<String, String> next) {
                return next.invoke("forwarded: " + context);
            }
        }
    }

    @Nested
    @DisplayName("Reflection-based handle method")
    class ReflectionHandleTests {

        @Test
        @DisplayName("should invoke handle method via reflection")
        void shouldInvokeHandleMethodViaReflection() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(HandleMethodBehavior.class);

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("handle: input");
        }

        @Test
        @DisplayName("should invoke handleAsync method via reflection")
        void shouldInvokeHandleAsyncMethodViaReflection() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(HandleAsyncMethodBehavior.class);

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("handleAsync: input");
        }

        @Test
        @DisplayName("should complete with null when handle returns non-CompletionStage")
        void shouldCompleteWithNullWhenHandleReturnsNonCompletionStage() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(NonCompletionStageHandleStep.class);

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isNull();
        }

        static class HandleMethodBehavior {
            private final PipelineDelegate<String, String> next;

            @SuppressWarnings("unused")
            public HandleMethodBehavior(PipelineDelegate<String, String> next) {
                this.next = next;
            }

            public CompletionStage<String> handle(String context) {
                return CompletableFuture.completedFuture("handle: " + context);
            }
        }

        static class HandleAsyncMethodBehavior {
            private final PipelineDelegate<String, String> next;

            @SuppressWarnings("unused")
            public HandleAsyncMethodBehavior(PipelineDelegate<String, String> next) {
                this.next = next;
            }

            public CompletionStage<String> handleAsync(String context) {
                return CompletableFuture.completedFuture("handleAsync: " + context);
            }
        }

        static class NonCompletionStageHandleStep {
            @SuppressWarnings("unused")
            public NonCompletionStageHandleStep(PipelineDelegate<String, String> next) {
            }

            public CompletionStage<String> handle(String context) {
                System.out.println("processed: " + context);
                return null;
            }
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should throw IllegalStateException when no handle method found")
        void shouldThrowIllegalStateExceptionWhenNoHandleMethodFound() {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(NoHandleMethodClass.class);

            assertThatThrownBy(() -> pipeline.build())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should throw IllegalStateException when multiple handle methods found")
        void shouldThrowIllegalStateExceptionWhenMultipleHandleMethodsFound() {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(DualHandleMethodClass.class);

            assertThatThrownBy(() -> pipeline.build())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should return failed future when handle method throws exception")
        void shouldReturnFailedFutureWhenHandleMethodThrowsException() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(ThrowingHandleStep.class);

            var future = pipeline.build().invoke("input").toCompletableFuture();

            assertThat(future).isCompletedExceptionally();
        }

        static class NoHandleMethodClass {
            @SuppressWarnings("unused")
            public NoHandleMethodClass(PipelineDelegate<String, String> next) {
            }

            public void doSomething() {
            }
        }

        static class DualHandleMethodClass {
            @SuppressWarnings("unused")
            public DualHandleMethodClass(PipelineDelegate<String, String> next) {
            }

            public CompletionStage<String> handle(String context) {
                return CompletableFuture.completedFuture("from handle");
            }

            public CompletionStage<String> handleAsync(String context, String extra) {
                return CompletableFuture.completedFuture("from handleAsync");
            }
        }

        static class ThrowingHandleStep {
            @SuppressWarnings("unused")
            public ThrowingHandleStep(PipelineDelegate<String, String> next) {
            }

            public CompletionStage<String> handle(String context) {
                throw new RuntimeException("handle failed");
            }
        }
    }

    @Nested
    @DisplayName("Constructor arguments")
    class ConstructorArgTests {

        @Test
        @DisplayName("should pass additional constructor args to the behavior instance")
        void shouldPassAdditionalConstructorArgsToBehaviorInstance() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(SingleCtorBehavior.class, "named");

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("named: input");
        }

        @Test
        @DisplayName("should prepend next delegate to constructor args")
        void shouldPrependNextDelegateToConstructorArgs() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);
            pipeline.use(CapturingNextBehavior.class);

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isEqualTo("captured: input");
        }

        static class SingleCtorBehavior {
            private final PipelineDelegate<String, String> next;
            private final String name;

            @SuppressWarnings("unused")
            public SingleCtorBehavior(PipelineDelegate<String, String> next, String name) {
                this.next = next;
                this.name = name;
            }

            public CompletionStage<String> handle(String context) {
                return CompletableFuture.completedFuture(name + ": " + context);
            }
        }

        static class CapturingNextBehavior {
            private final PipelineDelegate<String, String> next;

            @SuppressWarnings("unused")
            public CapturingNextBehavior(PipelineDelegate<String, String> next) {
                this.next = next;
            }

            public CompletionStage<String> handle(String context) {
                return CompletableFuture.completedFuture("captured: " + context);
            }
        }
    }

    @Nested
    @DisplayName("Default fallback")
    class DefaultFallbackTests {

        @Test
        @DisplayName("should return null when built without components")
        void shouldReturnNullWhenBuiltWithoutComponents() throws Exception {
            var provider = new SimpleServiceProvider();
            var pipeline = new DefaultPipelineProvider<String, String>(provider);

            var result = pipeline.build().invoke("input").toCompletableFuture().get();

            assertThat(result).isNull();
        }
    }

    class SimpleServiceProvider implements ServiceProvider{
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
