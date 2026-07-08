package com.euonia.reflection;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link MissingMethodException} 和 {@link AmbiguousMethodException}。
 */
@SuppressWarnings("unused")
@DisplayName("Osba Exceptions")
class OsbaExceptionsTest {

    @Nested
    @DisplayName("MissingMethodException")
    class MissingMethod {

        @Test
        @DisplayName("should store type name and method name")
        void shouldStoreTypeAndMethodName() {
            var ex = new MissingMethodException("com.example.MyClass", "findById");

            assertThat(ex.getTypeName()).isEqualTo("com.example.MyClass");
            assertThat(ex.getMethodName()).isEqualTo("findById");
            assertThat(ex.getMessage()).contains("findById");
            assertThat(ex.getMessage()).contains("MyClass");
        }

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            var ex = new MissingMethodException("Type", "method");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("AmbiguousMethodException")
    class AmbiguousMethod {

        @Test
        @DisplayName("should create with message")
        void shouldCreateWithMessage() {
            var ex = new AmbiguousMethodException("multiple matches found");

            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex.getMessage()).isEqualTo("multiple matches found");
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            var cause = new RuntimeException("root");
            var ex = new AmbiguousMethodException("ambiguous", cause);

            assertThat(ex.getMessage()).isEqualTo("ambiguous");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}
