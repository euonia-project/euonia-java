package com.euonia.reflection;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link PropertyInfo}。
 */
@SuppressWarnings("unused")
@DisplayName("PropertyInfo")
class PropertyInfoTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should store type and name")
        void shouldStoreTypeAndName() {
            var prop = new PropertyInfo<>(String.class, "email");

            assertThat(prop.getName()).isEqualTo("email");
            assertThat(prop.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("should store friendly name and default value")
        void shouldStoreFriendlyNameAndDefaultValue() {
            var prop = new PropertyInfo<>(String.class, "email", "Email Address", () -> "default@example.com");

            assertThat(prop.getFriendlyName()).isEqualTo("Email Address");
            assertThat(prop.getDefaultValue()).isEqualTo("default@example.com");
        }

        @Test
        @DisplayName("should return null default when no supplier")
        void shouldReturnNullDefaultWhenNoSupplier() {
            var prop = new PropertyInfo<>(Integer.class, "age");

            assertThat(prop.getDefaultValue()).isNull();
        }

        @Test
        @DisplayName("should return name as friendly name when not set")
        void shouldReturnNameAsFriendlyNameWhenNotSet() {
            var prop = new PropertyInfo<>(String.class, "email");

            assertThat(prop.getFriendlyName()).isEqualTo("email");
        }
    }

    @Nested
    @DisplayName("isChild")
    class IsChild {

        @Test
        @DisplayName("should return false for non-BusinessObject types")
        void shouldReturnFalseForNonBusinessObjectTypes() {
            var prop = new PropertyInfo<>(String.class, "value");

            assertThat(prop.isChild()).isFalse();
        }
    }

    @Nested
    @DisplayName("compareTo")
    class CompareTo {

        @Test
        @DisplayName("should compare by name")
        void shouldCompareByName() {
            var a = new PropertyInfo<>(String.class, "alpha");
            var b = new PropertyInfo<>(String.class, "beta");

            assertThat(a.compareTo(b)).isNegative();
            assertThat(b.compareTo(a)).isPositive();
            assertThat(a.compareTo(a)).isZero();
        }
    }

    @Nested
    @DisplayName("newFieldData")
    class NewFieldData {

        @Test
        @DisplayName("should create FieldData with given name")
        void shouldCreateFieldDataWithName() {
            var prop = new PropertyInfo<>(String.class, "name");

            var fieldData = prop.newFieldData("displayName");

            assertThat(fieldData).isNotNull();
            assertThat(fieldData.getName()).isEqualTo("displayName");
        }
    }
}
