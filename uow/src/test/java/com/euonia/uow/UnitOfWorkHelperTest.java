package com.euonia.uow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link UnitOfWorkHelper}。
 */
@SuppressWarnings("unused")
@DisplayName("UnitOfWorkHelper")
class UnitOfWorkHelperTest {

    @com.euonia.uow.annotation.UnitOfWork
    static class AnnotatedClass {}

    static class NonAnnotatedClass {}

    @Nested
    @DisplayName("isUnitOfWorkType")
    class IsUnitOfWorkType {

        @Test
        @DisplayName("should return true for annotated class")
        void shouldReturnTrueForAnnotatedClass() {
            assertThat(UnitOfWorkHelper.isUnitOfWorkType(AnnotatedClass.class)).isTrue();
        }

        @Test
        @DisplayName("should return false for non-annotated class")
        void shouldReturnFalseForNonAnnotatedClass() {
            assertThat(UnitOfWorkHelper.isUnitOfWorkType(NonAnnotatedClass.class)).isFalse();
        }

        @Test
        @DisplayName("should throw for null type")
        void shouldThrowForNullType() {
            assertThatThrownBy(() -> UnitOfWorkHelper.isUnitOfWorkType(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getUnitOfWorkAnnotation")
    class GetUnitOfWorkAnnotationTests {

        @Test
        @DisplayName("should throw for null method")
        void shouldThrowForNullMethod() {
            assertThatThrownBy(() -> UnitOfWorkHelper.getUnitOfWorkAnnotation(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
