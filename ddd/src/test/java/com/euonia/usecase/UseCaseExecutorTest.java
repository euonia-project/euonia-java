package com.euonia.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.application.StubServiceProvider;

/**
 * 测试 {@link DefaultUseCaseExecutor}。
 */
@SuppressWarnings("unused")
@DisplayName("UseCaseExecutor")
class UseCaseExecutorTest {

    static class AddUseCase implements UseCase<Integer, Integer> {
        @Override
        public CompletionStage<Integer> executeAsync(Integer input) {
            return CompletableFuture.completedFuture(input + 1);
        }
    }

    static class ThrowingUseCase implements UseCase<String, String> {
        @Override
        public CompletionStage<String> executeAsync(String input) {
            throw new IllegalArgumentException("sync failure");
        }
    }

    static class FailingUseCase implements UseCase<String, String> {
        @Override
        public CompletionStage<String> executeAsync(String input) {
            return CompletableFuture.failedFuture(new IllegalStateException("async failure"));
        }
    }

    static class LogUseCase implements NonOutputUseCase<String> {
        @Override
        public CompletionStage<Void> executeVoidAsync(String input) {
            return CompletableFuture.completedFuture(null);
        }
    }

    static class TimeUseCase implements NonInputUseCase<String> {
        @Override
        public CompletionStage<String> executeAsync() {
            return CompletableFuture.completedFuture("now");
        }
    }

    static class PingUseCase implements ParameterlessUseCase {
        @Override
        public CompletionStage<Void> executeVoidAsync() {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Nested
    @DisplayName("direct execution")
    class DirectExecution {

        @Test
        @DisplayName("should deliver output to presenter on success")
        void shouldDeliverOutputOnSuccess() {
            var presenter = new DefaultUseCasePresenter<Integer>();
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(new AddUseCase(), 1, presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isEqualTo(2);
        }

        @Test
        @DisplayName("should deliver error to presenter when use case fails asynchronously")
        void shouldDeliverErrorOnAsyncFailure() {
            var presenter = new DefaultUseCasePresenter<String>();
            var error = new AtomicReference<Throwable>();
            presenter.onFailed(error::set);
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(new FailingUseCase(), "input", presenter).toCompletableFuture().join();

            assertThat(error.get()).isInstanceOf(IllegalStateException.class)
                                   .hasMessage("async failure");
        }

        @Test
        @DisplayName("should deliver error to presenter when use case throws synchronously")
        void shouldDeliverErrorOnSyncFailure() {
            var presenter = new DefaultUseCasePresenter<String>();
            var error = new AtomicReference<Throwable>();
            presenter.onFailed(error::set);
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(new ThrowingUseCase(), "input", presenter).toCompletableFuture().join();

            assertThat(error.get()).isInstanceOf(IllegalArgumentException.class)
                                   .hasMessage("sync failure");
        }

        @Test
        @DisplayName("should notify canceled listeners on cancellation")
        void shouldNotifyCanceledListeners() {
            var presenter = new DefaultUseCasePresenter<String>();
            var canceled = new AtomicReference<Boolean>();
            presenter.onCanceled(() -> canceled.set(true));
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            UseCase<String, String> useCase = input -> CompletableFuture.failedFuture(new CancellationException("canceled"));

            executor.executeAsync(useCase, "input", presenter).toCompletableFuture().join();

            assertThat(canceled.get()).isTrue();
        }

        @Test
        @DisplayName("should adapt non-output use case to empty output")
        void shouldAdaptNonOutputUseCase() {
            var presenter = new DefaultUseCasePresenter<EmptyUseCaseOutput>();
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(new LogUseCase(), "input", presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isSameAs(EmptyUseCaseOutput.INSTANCE);
        }

        @Test
        @DisplayName("should execute non-input use case")
        void shouldExecuteNonInputUseCase() {
            var presenter = new DefaultUseCasePresenter<String>();
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(new TimeUseCase(), presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isEqualTo("now");
        }

        @Test
        @DisplayName("should execute parameterless use case")
        void shouldExecuteParameterlessUseCase() {
            var presenter = new DefaultUseCasePresenter<EmptyUseCaseOutput>();
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(new PingUseCase(), presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isSameAs(EmptyUseCaseOutput.INSTANCE);
        }
    }

    @Nested
    @DisplayName("container resolution")
    class ContainerResolution {

        @Test
        @DisplayName("should resolve and execute typed use case")
        void shouldResolveAndExecuteTypedUseCase() {
            var presenter = new DefaultUseCasePresenter<Integer>();
            var provider = StubServiceProvider.with(AddUseCase.class, new AddUseCase());
            var executor = new DefaultUseCaseExecutor(provider);

            executor.executeAsync(AddUseCase.class, 41, presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isEqualTo(42);
        }

        @Test
        @DisplayName("should resolve and execute non-output use case")
        void shouldResolveAndExecuteNonOutputUseCase() {
            var presenter = new DefaultUseCasePresenter<EmptyUseCaseOutput>();
            var provider = StubServiceProvider.with(LogUseCase.class, new LogUseCase());
            var executor = new DefaultUseCaseExecutor(provider);

            executor.executeNonOutputAsync(LogUseCase.class, "input", presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isSameAs(EmptyUseCaseOutput.INSTANCE);
        }

        @Test
        @DisplayName("should resolve and execute non-input use case")
        void shouldResolveAndExecuteNonInputUseCase() {
            var presenter = new DefaultUseCasePresenter<String>();
            var provider = StubServiceProvider.with(TimeUseCase.class, new TimeUseCase());
            var executor = new DefaultUseCaseExecutor(provider);

            executor.executeNonInputAsync(TimeUseCase.class, presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isEqualTo("now");
        }

        @Test
        @DisplayName("should resolve and execute parameterless use case")
        void shouldResolveAndExecuteParameterlessUseCase() {
            var presenter = new DefaultUseCasePresenter<EmptyUseCaseOutput>();
            var provider = StubServiceProvider.with(PingUseCase.class, new PingUseCase());
            var executor = new DefaultUseCaseExecutor(provider);

            executor.executeParameterlessAsync(PingUseCase.class, presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isSameAs(EmptyUseCaseOutput.INSTANCE);
        }
    }
}
