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
 * 测试 {@link UseCase} 接口和 {@link DefaultUseCasePresenter}。
 */
@SuppressWarnings("unused")
@DisplayName("UseCase / UseCasePresenter")
class UseCasePresenterTest {

    static class GreetUseCase implements UseCase<String, String> {
        @Override
        public CompletionStage<String> executeAsync(String name) {
            return CompletableFuture.completedFuture("Hello, " + name + "!");
        }
    }

    @Nested
    @DisplayName("success")
    class Success {

        @Test
        @DisplayName("should notify success subscribers")
        void shouldNotifySuccessSubscribers() {
            var presenter = new DefaultUseCasePresenter<String>();
            var first = new AtomicReference<String>();
            var second = new AtomicReference<String>();

            presenter.onSucceed(first::set);
            presenter.onSucceed(second::set);

            presenter.ok("Hello, World!");

            assertThat(first.get()).isEqualTo("Hello, World!");
            assertThat(second.get()).isEqualTo("Hello, World!");
            assertThat(presenter.getOutput()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("should support subscribe convenience method")
        void shouldSupportSubscribeConvenienceMethod() {
            var presenter = new DefaultUseCasePresenter<String>();
            var result = new AtomicReference<String>();

            presenter.subscribe(result::set, error -> {});

            presenter.ok("result");

            assertThat(result.get()).isEqualTo("result");
        }
    }

    @Nested
    @DisplayName("failure")
    class Failure {

        @Test
        @DisplayName("should notify failure subscribers")
        void shouldNotifyFailureSubscribers() {
            var presenter = new DefaultUseCasePresenter<String>();
            var error = new AtomicReference<Throwable>();

            presenter.onFailed(error::set);

            var ex = new RuntimeException("test error");
            presenter.error(ex);

            assertThat(error.get()).isSameAs(ex);
            assertThat(presenter.getOutput()).isNull();
        }

        @Test
        @DisplayName("should notify canceled listeners for cancellation exceptions")
        void shouldNotifyCanceledListenersForCancellationExceptions() {
            var presenter = new DefaultUseCasePresenter<String>();
            var canceled = new AtomicReference<Boolean>();
            var failed = new AtomicReference<Throwable>();

            presenter.onCanceled(() -> canceled.set(true));
            presenter.onFailed(failed::set);

            presenter.error(new CancellationException("canceled"));

            assertThat(canceled.get()).isTrue();
            assertThat(failed.get()).isNull();
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("should clear listeners on close")
        void shouldClearListenersOnClose() {
            var presenter = new DefaultUseCasePresenter<String>();
            var result = new AtomicReference<String>();

            presenter.onSucceed(result::set);
            presenter.close();
            presenter.ok("ignored");

            assertThat(result.get()).isNull();
            assertThat(presenter.getOutput()).isEqualTo("ignored");
        }
    }

    @Nested
    @DisplayName("UseCase integration")
    class UseCaseIntegration {

        @Test
        @DisplayName("should execute use case and deliver result via presenter")
        void shouldExecuteAndDeliverViaPresenter() {
            var useCase = new GreetUseCase();
            var presenter = new DefaultUseCasePresenter<String>();
            var executor = new DefaultUseCaseExecutor(new StubServiceProvider());

            executor.executeAsync(useCase, "Alice", presenter).toCompletableFuture().join();

            assertThat(presenter.getOutput()).isEqualTo("Hello, Alice!");
        }
    }
}
