package com.euonia.domain.auditing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link AuditRecord}。
 */
@SuppressWarnings("unused")
@DisplayName("AuditRecord")
class AuditRecordTest {

    @Nested
    @DisplayName("4-arg constructor")
    class FourArgConstructor {

        @Test
        @DisplayName("should store entity name, entity id, action, and timestamp")
        void shouldStoreAllFields() {
            var ts = Instant.now();
            var record = new AuditRecord<>("Order", "order-1", "CREATE", ts);

            assertThat(record.getEntityName()).isEqualTo("Order");
            assertThat(record.getEntityId()).isEqualTo("order-1");
            assertThat(record.getAction()).isEqualTo("CREATE");
            assertThat(record.getTimestamp()).isEqualTo(ts);
        }
    }

    @Nested
    @DisplayName("3-arg constructor (auto timestamp)")
    class ThreeArgConstructor {

        @Test
        @DisplayName("should auto-set timestamp to current time")
        void shouldAutoSetTimestamp() {
            var before = Instant.now();
            var record = new AuditRecord<>("Order", "order-1", "UPDATE");
            var after = Instant.now();

            assertThat(record.getEntityName()).isEqualTo("Order");
            assertThat(record.getEntityId()).isEqualTo("order-1");
            assertThat(record.getAction()).isEqualTo("UPDATE");
            assertThat(record.getTimestamp()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("Class-based constructor")
    class ClassBasedConstructor {

        @Test
        @DisplayName("should extract entity name from class simple name")
        void shouldExtractEntityNameFromClass() {
            var record = new AuditRecord<>(AuditRecord.class, "id-1", "DELETE");

            assertThat(record.getEntityName()).isEqualTo("AuditRecord");
            assertThat(record.getEntityId()).isEqualTo("id-1");
            assertThat(record.getAction()).isEqualTo("DELETE");
        }
    }

    @Nested
    @DisplayName("user fields")
    class UserFields {

        @Test
        @DisplayName("should set and get user id")
        void shouldSetAndGetUserId() {
            var record = new AuditRecord<>("Order", "o-1", "CREATE");
            record.setUserId("user-123");

            assertThat(record.getUserId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("should set and get user name")
        void shouldSetAndGetUserName() {
            var record = new AuditRecord<>("Order", "o-1", "CREATE");
            record.setUserName("John Doe");

            assertThat(record.getUserName()).isEqualTo("John Doe");
        }
    }

    @Nested
    @DisplayName("comment")
    class Comment {

        @Test
        @DisplayName("should set and get comment")
        void shouldSetAndGetComment() {
            var record = new AuditRecord<>("Order", "o-1", "UPDATE");
            record.setComment("Updated status to shipped");

            assertThat(record.getComment()).isEqualTo("Updated status to shipped");
        }
    }

    @Nested
    @DisplayName("entity inheritance")
    class EntityInheritance {

        @Test
        @DisplayName("should extend EntityBase")
        void shouldExtendEntityBase() {
            var record = new AuditRecord<>("Order", "o-1", "CREATE");

            assertThat(record).isInstanceOf(com.euonia.domain.EntityBase.class);
        }

        @Test
        @DisplayName("should support id from EntityBase")
        void shouldSupportId() {
            var record = new AuditRecord<Long>("Order", "o-1", "CREATE");
            record.setId(100L);

            assertThat(record.getId()).isEqualTo(100L);
            assertThat(record.getKeys()).containsExactly(100L);
        }
    }
}
