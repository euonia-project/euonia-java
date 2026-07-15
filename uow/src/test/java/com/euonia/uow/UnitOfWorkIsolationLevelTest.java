package com.euonia.uow;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link UnitOfWorkIsolationLevel}。
 */
@SuppressWarnings("unused")
@DisplayName("UnitOfWorkIsolationLevel")
class UnitOfWorkIsolationLevelTest {

    @Nested
    @DisplayName("jdbc mapping")
    class JdbcMapping {

        @Test
        @DisplayName("should map UNSPECIFIED to TRANSACTION_NONE")
        void shouldMapUnspecifiedToNone() {
            assertThat(UnitOfWorkIsolationLevel.UNSPECIFIED.toJdbcIsolationLevel())
                .isEqualTo(Connection.TRANSACTION_NONE);
        }

        @Test
        @DisplayName("should map READ_UNCOMMITTED correctly")
        void shouldMapReadUncommitted() {
            assertThat(UnitOfWorkIsolationLevel.READ_UNCOMMITTED.toJdbcIsolationLevel())
                .isEqualTo(Connection.TRANSACTION_READ_UNCOMMITTED);
        }

        @Test
        @DisplayName("should map READ_COMMITTED correctly")
        void shouldMapReadCommitted() {
            assertThat(UnitOfWorkIsolationLevel.READ_COMMITTED.toJdbcIsolationLevel())
                .isEqualTo(Connection.TRANSACTION_READ_COMMITTED);
        }

        @Test
        @DisplayName("should map REPEATABLE_READ correctly")
        void shouldMapRepeatableRead() {
            assertThat(UnitOfWorkIsolationLevel.REPEATABLE_READ.toJdbcIsolationLevel())
                .isEqualTo(Connection.TRANSACTION_REPEATABLE_READ);
        }

        @Test
        @DisplayName("should map SERIALIZABLE correctly")
        void shouldMapSerializable() {
            assertThat(UnitOfWorkIsolationLevel.SERIALIZABLE.toJdbcIsolationLevel())
                .isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
        }
    }

    @Nested
    @DisplayName("enum values")
    class EnumValues {

        @Test
        @DisplayName("should contain all 7 isolation levels")
        void shouldContainAllSevenLevels() {
            assertThat(UnitOfWorkIsolationLevel.values())
                .containsExactly(
                    UnitOfWorkIsolationLevel.UNSPECIFIED,
                    UnitOfWorkIsolationLevel.CHAOS,
                    UnitOfWorkIsolationLevel.READ_UNCOMMITTED,
                    UnitOfWorkIsolationLevel.READ_COMMITTED,
                    UnitOfWorkIsolationLevel.REPEATABLE_READ,
                    UnitOfWorkIsolationLevel.SNAPSHOT,
                    UnitOfWorkIsolationLevel.SERIALIZABLE
                );
        }
    }
}
