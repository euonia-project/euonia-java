package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MessageMetadata} 的 Map 委托和类型安全获取方法。
 */
@SuppressWarnings("unused")
@DisplayName("MessageMetadata")
class MessageMetadataTest {

    @Nested
    @DisplayName("Map delegate")
    class MapDelegate {

        @Test
        @DisplayName("should initially be empty")
        void shouldBeEmptyInitially() {
            var md = new MessageMetadata();
            assertThat(md).isEmpty();
            assertThat(md.size()).isZero();
        }

        @Test
        @DisplayName("should put and get values")
        void shouldPutAndGet() {
            var md = new MessageMetadata();
            md.put("key", "value");

            assertThat(md).containsKey("key");
            assertThat(md.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("should remove values")
        void shouldRemoveValues() {
            var md = new MessageMetadata();
            md.put("key", "value");
            md.remove("key");

            assertThat(md).doesNotContainKey("key");
        }

        @Test
        @DisplayName("should report size correctly")
        void shouldReportSize() {
            var md = new MessageMetadata();
            md.put("a", 1);
            md.put("b", 2);

            assertThat(md).hasSize(2);
        }

        @Test
        @DisplayName("should clear all entries")
        void shouldClear() {
            var md = new MessageMetadata();
            md.put("a", 1);
            md.clear();

            assertThat(md).isEmpty();
        }

        @Test
        @DisplayName("should support putAll")
        void shouldSupportPutAll() {
            var md = new MessageMetadata();
            md.putAll(java.util.Map.of("a", 1, "b", 2));

            assertThat(md).hasSize(2);
        }

        @Test
        @DisplayName("should track containsValue")
        void shouldTrackContainsValue() {
            var md = new MessageMetadata();
            md.put("a", 42);

            assertThat(md.containsValue(42)).isTrue();
            assertThat(md.containsValue(99)).isFalse();
        }
    }

    @Nested
    @DisplayName("typed get")
    class TypedGet {

        @Test
        @DisplayName("should return typed value")
        void shouldReturnTypedValue() {
            var md = new MessageMetadata();
            md.put("num", 42);

            assertThat(md.get("num", Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("should return null for missing key (typed)")
        void shouldReturnNullForMissingKeyTyped() {
            var md = new MessageMetadata();

            assertThat(md.get("nope", String.class)).isNull();
        }

        @Test
        @DisplayName("should throw for type mismatch")
        void shouldThrowForTypeMismatch() {
            var md = new MessageMetadata();
            md.put("key", "string");

            assertThatThrownBy(() -> md.get("key", Integer.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("key");
        }
    }

    @Nested
    @DisplayName("string key get")
    class StringKeyGet {

        @Test
        @DisplayName("should return value by string key")
        void shouldGetByStringKey() {
            var md = new MessageMetadata();
            md.put("hello", "world");

            assertThat(md.get("hello")).isEqualTo("world");
        }

        @Test
        @DisplayName("should return null for missing string key")
        void shouldReturnNullForMissingStringKey() {
            var md = new MessageMetadata();

            assertThat(md.get("missing")).isNull();
        }
    }
}
