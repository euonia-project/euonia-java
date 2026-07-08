package com.euonia.uow;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link UnitOfWorkEvent} 和 {@link UnitOfWorkFailure}。
 */
@SuppressWarnings("unused")
@DisplayName("UnitOfWorkEvent / UnitOfWorkFailure")
class UnitOfWorkEventTest {

    @Nested
    @DisplayName("UnitOfWorkEvent")
    class EventTests {

        @Test
        @DisplayName("should store unit of work reference")
        void shouldStoreUnitOfWorkReference() {
            try (var uow = new UnitOfWork()) {
                var event = new UnitOfWorkEvent(uow);

                assertThat(event.getUnitOfWork()).isSameAs(uow);
            }
        }
    }

    @Nested
    @DisplayName("UnitOfWorkFailure")
    class FailureTests {

        @Test
        @DisplayName("should store exception and rollback flag")
        void shouldStoreExceptionAndRollbackFlag() {
            try (var uow = new UnitOfWork()) {
                var ex = new RuntimeException("failure");
                var failure = new UnitOfWorkFailure(uow, ex, true);

                assertThat(failure.getUnitOfWork()).isSameAs(uow);
                assertThat(failure.getException()).isSameAs(ex);
                assertThat(failure.isRollback()).isTrue();
            }
        }

        @Test
        @DisplayName("should extend UnitOfWorkEvent")
        void shouldExtendUnitOfWorkEvent() {
            try (var uow = new UnitOfWork()) {
                var failure = new UnitOfWorkFailure(uow, null, false);

                assertThat(failure).isInstanceOf(UnitOfWorkEvent.class);
            }
        }

        @Test
        @DisplayName("should allow null exception (explicit rollback)")
        void shouldAllowNullException() {
            try (var uow = new UnitOfWork()) {
                var failure = new UnitOfWorkFailure(uow, null, true);

                assertThat(failure.getException()).isNull();
                assertThat(failure.isRollback()).isTrue();
            }
        }
    }
}
