package com.euonia.osba;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link SavedEventArgs}。
 */
@SuppressWarnings("unused")
@DisplayName("SavedEventArgs")
class SavedEventArgsTest {

    @Nested
    @DisplayName("single-arg constructor")
    class SingleArgConstructor {

        @Test
        @DisplayName("should store new object")
        void shouldStoreNewObject() {
            var args = new SavedEventArgs("saved-obj");

            assertThat(args.getNewObject()).isEqualTo("saved-obj");
            assertThat(args.getError()).isNull();
            assertThat(args.getUserState()).isNull();
        }
    }

    @Nested
    @DisplayName("three-arg constructor")
    class ThreeArgConstructor {

        @Test
        @DisplayName("should store all fields")
        void shouldStoreAllFields() {
            var error = new RuntimeException("save error");
            var args = new SavedEventArgs("saved-obj", error, "user-state");

            assertThat(args.getNewObject()).isEqualTo("saved-obj");
            assertThat(args.getError()).isSameAs(error);
            assertThat(args.getUserState()).isEqualTo("user-state");
        }
    }
}
