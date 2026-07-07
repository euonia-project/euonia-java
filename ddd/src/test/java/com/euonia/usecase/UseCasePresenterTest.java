package com.euonia.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link UseCase} 接口和 {@link UseCasePresenter}。
 */
@SuppressWarnings("unused")
@DisplayName("UseCase / UseCasePresenter")
class UseCasePresenterTest {

    static class GreetUseCase implements UseCase<String, String> {
        @Override
        public String execute(String name) {
            return "Hello, " + name + "!";
        }
    }

    @Nested
    @DisplayName("success")
    class Success {

        @Test
        @DisplayName("should notify success subscriber")
        void shouldNotifySuccessSubscriber() throws Exception {
            var presenter = new UseCasePresenter<String>();
            var result = new AtomicReference<String>();
            var latch = new CountDownLatch(1);

            presenter.subscribe(r -> { result.set(r); latch.countDown(); }, err -> {});

            presenter.success("Hello, World!");
            latch.await(2, TimeUnit.SECONDS);

            assertThat(result.get()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("should store output via getOutput")
        void shouldStoreOutput() {
            var presenter = new UseCasePresenter<String>();
            presenter.success("result");

            assertThat(presenter.getOutput()).isEqualTo("result");
        }
    }

    @Nested
    @DisplayName("failure")
    class Failure {

        @Test
        @DisplayName("should notify failure subscriber")
        void shouldNotifyFailureSubscriber() throws Exception {
            var presenter = new UseCasePresenter<String>();
            var error = new AtomicReference<Throwable>();
            var latch = new CountDownLatch(1);

            presenter.subscribe(r -> {}, err -> { error.set(err); latch.countDown(); });

            var ex = new RuntimeException("test error");
            presenter.error(ex);
            latch.await(2, TimeUnit.SECONDS);

            assertThat(error.get()).isSameAs(ex);
        }
    }

    @Nested
    @DisplayName("UseCase integration")
    class UseCaseIntegration {

        @Test
        @DisplayName("should execute use case and deliver result via presenter")
        void shouldExecuteAndDeliverViaPresenter() throws Exception {
            var useCase = new GreetUseCase();
            var presenter = new UseCasePresenter<String>();
            var result = new AtomicReference<String>();
            var latch = new CountDownLatch(1);

            presenter.subscribe(r -> { result.set(r); latch.countDown(); }, err -> {});

            var output = useCase.execute("Alice");
            presenter.success(output);
            latch.await(2, TimeUnit.SECONDS);

            assertThat(result.get()).isEqualTo("Hello, Alice!");
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("should close without error")
        void shouldCloseWithoutError() throws Exception {
            var presenter = new UseCasePresenter<String>();
            presenter.close();
            // No exception thrown
        }
    }
}
