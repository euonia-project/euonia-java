package com.euonia.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link ValueObject} 的 equals、hashCode、compareTo。
 */
@SuppressWarnings("unused")
@DisplayName("ValueObject")
class ValueObjectTest {

    static class Money extends ValueObject<Money> {
        final String currency;
        final int amount;

        Money(String currency, int amount) {
            this.currency = currency;
            this.amount = amount;
        }
    }

    static class PersonName extends ValueObject<PersonName> {
        final String first;
        final String last;

        PersonName(String first, String last) {
            this.first = first;
            this.last = last;
        }
    }

    @Nested
    @DisplayName("equals")
    class Equals {

        @Test
        @DisplayName("should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            var a = new Money("USD", 100);
            var b = new Money("USD", 100);

            assertThat(a.equals(b)).isTrue();
        }

        @Test
        @DisplayName("should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            var a = new Money("USD", 100);
            var b = new Money("EUR", 100);

            assertThat(a.equals(b)).isFalse();
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            var a = new Money("USD", 100);

            assertThat(a.equals(null)).isFalse();
        }

        @Test
        @DisplayName("should be reflexive")
        void shouldBeReflexive() {
            var a = new Money("USD", 100);

            assertThat(a.equals(a)).isTrue();
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            var a = new Money("USD", 100);
            var b = new PersonName("John", "Doe");

            assertThat(a.equals(b)).isFalse();
        }

        @Test
        @DisplayName("should correctly identify equal via standard equals")
        void shouldCorrectlyIdentifyEqualViaStandardEquals() {
            var a = new Money("USD", 100);
            var b = new Money("USD", 100);

            assertThat(a).isEqualTo(b);
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("should be consistent for equal objects")
        void shouldBeConsistentForEqualObjects() {
            var a = new Money("USD", 100);
            var b = new Money("USD", 100);

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("should differ for different objects")
        void shouldDifferForDifferentObjects() {
            var a = new Money("USD", 100);
            var b = new Money("EUR", 200);

            // different values typically produce different hash codes
            assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("compareTo")
    class CompareTo {

        @Test
        @DisplayName("should return positive when comparing to null")
        void shouldReturnPositiveWhenComparingToNull() {
            var a = new Money("USD", 100);

            assertThat(a.compareTo(null)).isPositive();
        }

        @Test
        @DisplayName("should return zero when all fields equal")
        void shouldReturnZeroWhenAllFieldsEqual() {
            var a = new Money("USD", 100);
            var b = new Money("USD", 100);

            assertThat(a.compareTo(b)).isZero();
        }

        @Test
        @DisplayName("should compare by first differing field")
        void shouldCompareByFirstDifferingField() {
            var a = new Money("EUR", 100);
            var b = new Money("USD", 100);

            // "EUR" < "USD" lexicographically
            assertThat(a.compareTo(b)).isNegative();
            assertThat(b.compareTo(a)).isPositive();
        }

        @Test
        @DisplayName("should handle null field - null sorts before non-null")
        void shouldHandleNullField() {
            var a = new Money(null, 100);
            var b = new Money("USD", 100);

            assertThat(a.compareTo(b)).isNegative();
        }
    }

    @Nested
    @DisplayName("multiple fields")
    class MultipleFields {

        @Test
        @DisplayName("should compare all fields for PersonName")
        void shouldCompareAllFieldsForPersonName() {
            var a = new PersonName("Alice", "Smith");
            var b = new PersonName("Alice", "Smith");
            var c = new PersonName("Bob", "Smith");

            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a.compareTo(c)).isNegative();
        }
    }
}
