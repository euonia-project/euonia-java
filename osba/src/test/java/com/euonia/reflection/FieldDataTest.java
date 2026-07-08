package com.euonia.reflection;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link FieldData}。
 */
@SuppressWarnings("unused")
@DisplayName("FieldData")
class FieldDataTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should default name to null")
        void shouldDefaultNameToNull() {
            var fd = new FieldData<>();

            assertThat(fd.getName()).isNull();
        }

        @Test
        @DisplayName("should accept name in constructor")
        void shouldAcceptName() {
            var fd = new FieldData<>("username");

            assertThat(fd.getName()).isEqualTo("username");
        }
    }

    @Nested
    @DisplayName("value management")
    class ValueManagement {

        @Test
        @DisplayName("should default value to null")
        void shouldDefaultValueToNull() {
            var fd = new FieldData<String>();

            assertThat(fd.getValue()).isNull();
        }

        @Test
        @DisplayName("should set and get value")
        void shouldSetAndGetValue() {
            var fd = new FieldData<String>();
            fd.setValue("hello");

            assertThat(fd.getValue()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should track old value in history")
        void shouldTrackOldValueInHistory() {
            var fd = new FieldData<>("name");
            fd.setValue("first");
            fd.setValue("second");

            assertThat(fd.getValue()).isEqualTo("second");
        }

        @Test
        @DisplayName("should support multiple value types")
        void shouldSupportMultipleValueTypes() {
            var intFd = new FieldData<>("count");
            intFd.setValue(42);
            assertThat(intFd.getValue()).isEqualTo(42);

            var boolFd = new FieldData<>("enabled");
            boolFd.setValue(true);
            assertThat(boolFd.getValue()).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("change tracking")
    class ChangeTracking {

        @Test
        @DisplayName("should not be changed initially")
        void shouldNotBeChangedInitially() {
            var fd = new FieldData<>("prop");

            assertThat(fd.isChanged()).isFalse();
        }

        @Test
        @DisplayName("should be changed after setting value")
        void shouldBeChangedAfterSettingValue() throws Exception {
            var fd = new FieldData<>("prop");
            fd.setValue("updated");
            Thread.sleep(50);

            assertThat(fd.isChanged()).isTrue();
        }

        @Test
        @DisplayName("should mark as unchanged clearing history")
        void shouldMarkAsUnchanged() throws Exception {
            var fd = new FieldData<>("prop");
            fd.setValue("updated");
            Thread.sleep(50);
            fd.markAsUnchanged();

            assertThat(fd.isChanged()).isFalse();
        }

        @Test
        @DisplayName("should not be deleted for simple values")
        void shouldNotBeDeletedForSimpleValues() {
            var fd = new FieldData<>("prop");
            fd.setValue("value");

            assertThat(fd.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("should not be new for simple values")
        void shouldNotBeNewForSimpleValues() {
            var fd = new FieldData<>("prop");

            assertThat(fd.isNew()).isFalse();
        }
    }
}
