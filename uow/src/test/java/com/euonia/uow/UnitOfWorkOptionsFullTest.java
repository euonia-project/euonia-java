package com.euonia.uow;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link UnitOfWorkOptions}。
 */
@SuppressWarnings("unused")
@DisplayName("UnitOfWorkOptions")
class UnitOfWorkOptionsFullTest {

    @Nested
    @DisplayName("constructors")
    class Constructors {

        @Test
        @DisplayName("should default to non-transactional")
        void shouldDefaultToNonTransactional() {
            var opts = new UnitOfWorkOptions();

            assertThat(opts.isTransactional()).isFalse();
            assertThat(opts.getIsolationLevel()).isNull();
            assertThat(opts.getTimeout()).isNull();
        }

        @Test
        @DisplayName("should accept transactional flag")
        void shouldAcceptTransactionalFlag() {
            var opts = new UnitOfWorkOptions(true);

            assertThat(opts.isTransactional()).isTrue();
        }

        @Test
        @DisplayName("should accept full parameters")
        void shouldAcceptFullParameters() {
            var opts = new UnitOfWorkOptions(true, UnitOfWorkIsolationLevel.SERIALIZABLE, Duration.ofMinutes(1));

            assertThat(opts.isTransactional()).isTrue();
            assertThat(opts.getIsolationLevel()).isEqualTo(UnitOfWorkIsolationLevel.SERIALIZABLE);
            assertThat(opts.getTimeout()).isEqualTo(Duration.ofMinutes(1));
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("should set transactional flag")
        void shouldSetTransactionalFlag() {
            var opts = new UnitOfWorkOptions();
            opts.setTransactional(true);

            assertThat(opts.isTransactional()).isTrue();
        }

        @Test
        @DisplayName("should set isolation level")
        void shouldSetIsolationLevel() {
            var opts = new UnitOfWorkOptions();
            opts.setIsolationLevel(UnitOfWorkIsolationLevel.READ_COMMITTED);

            assertThat(opts.getIsolationLevel()).isEqualTo(UnitOfWorkIsolationLevel.READ_COMMITTED);
        }

        @Test
        @DisplayName("should set timeout")
        void shouldSetTimeout() {
            var opts = new UnitOfWorkOptions();
            opts.setTimeout(Duration.ofSeconds(10));

            assertThat(opts.getTimeout()).isEqualTo(Duration.ofSeconds(10));
        }
    }

    @Nested
    @DisplayName("normalize")
    class Normalize {

        @Test
        @DisplayName("should fill null isolation level from defaults")
        void shouldFillNullIsolationLevel() {
            var defaults = new UnitOfWorkOptions(true, UnitOfWorkIsolationLevel.READ_COMMITTED, Duration.ofSeconds(30));
            var opts = new UnitOfWorkOptions(false, null, Duration.ofSeconds(10));

            defaults.normalize(opts);

            assertThat(opts.getIsolationLevel()).isEqualTo(UnitOfWorkIsolationLevel.READ_COMMITTED);
            assertThat(opts.getTimeout()).isEqualTo(Duration.ofSeconds(10)); // should keep own value
        }

        @Test
        @DisplayName("should fill null timeout from defaults")
        void shouldFillNullTimeout() {
            var defaults = new UnitOfWorkOptions(true, null, Duration.ofSeconds(30));
            var opts = new UnitOfWorkOptions(false, UnitOfWorkIsolationLevel.READ_UNCOMMITTED, null);

            defaults.normalize(opts);

            assertThat(opts.getIsolationLevel()).isEqualTo(UnitOfWorkIsolationLevel.READ_UNCOMMITTED); // keep own
            assertThat(opts.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("should reject null options")
        void shouldRejectNullOptions() {
            var defaults = new UnitOfWorkOptions();

            assertThatThrownBy(() -> defaults.normalize(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
