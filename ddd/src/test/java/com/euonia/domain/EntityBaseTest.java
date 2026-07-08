package com.euonia.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link EntityBase}。
 */
@SuppressWarnings("unused")
@DisplayName("EntityBase")
class EntityBaseTest {

    static class TestEntity extends EntityBase<String> {}

    @Nested
    @DisplayName("id management")
    class IdManagement {

        @Test
        @DisplayName("should default id to null")
        void shouldDefaultIdToNull() {
            var entity = new TestEntity();

            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("should set and get id")
        void shouldSetAndGetId() {
            var entity = new TestEntity();
            entity.setId("entity-1");

            assertThat(entity.getId()).isEqualTo("entity-1");
        }
    }

    @Nested
    @DisplayName("keys")
    class Keys {

        @Test
        @DisplayName("should return array containing id")
        void shouldReturnArrayContainingId() {
            var entity = new TestEntity();
            entity.setId("key-1");

            assertThat(entity.getKeys()).containsExactly("key-1");
        }

        @Test
        @DisplayName("should return array with null when id not set")
        void shouldReturnArrayWithNullWhenIdNotSet() {
            var entity = new TestEntity();

            assertThat(entity.getKeys()).containsExactly((Object) null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include class simple name and id")
        void shouldIncludeClassNameAndId() {
            var entity = new TestEntity();
            entity.setId("123");

            assertThat(entity.toString()).isEqualTo("TestEntity{id=123}");
        }

        @Test
        @DisplayName("should show null id in toString")
        void shouldShowNullIdInToString() {
            var entity = new TestEntity();

            assertThat(entity.toString()).isEqualTo("TestEntity{id=null}");
        }
    }
}
