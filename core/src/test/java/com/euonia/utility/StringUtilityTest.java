package com.euonia.utility;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link StringUtility} 的单元测试。
 */
@DisplayName("StringUtility")
class StringUtilityTest {

    // ── isNullOrEmpty ────────────────────────────────────────────

    @Nested
    @DisplayName("isNullOrEmpty")
    class IsNullOrEmptyTests {

        @Test
        @DisplayName("Given null when isNullOrEmpty then return true")
        void givenNullWhenIsNullOrEmptyThenReturnTrue() {
            assertTrue(StringUtility.isNullOrEmpty(null));
        }

        @Test
        @DisplayName("Given empty string when isNullOrEmpty then return true")
        void givenEmptyStringWhenIsNullOrEmptyThenReturnTrue() {
            assertTrue(StringUtility.isNullOrEmpty(""));
        }

        @Test
        @DisplayName("Given non-empty string when isNullOrEmpty then return false")
        void givenNonEmptyStringWhenIsNullOrEmptyThenReturnFalse() {
            assertFalse(StringUtility.isNullOrEmpty("hello"));
        }

        @Test
        @DisplayName("Given blank string when isNullOrEmpty then return false")
        void givenBlankStringWhenIsNullOrEmptyThenReturnFalse() {
            assertFalse(StringUtility.isNullOrEmpty("   "));
        }
    }

    // ── isNullOrBlank ────────────────────────────────────────────

    @Nested
    @DisplayName("isNullOrBlank")
    class IsNullOrBlankTests {

        @Test
        @DisplayName("Given null when isNullOrBlank then return true")
        void givenNullWhenIsNullOrBlankThenReturnTrue() {
            assertTrue(StringUtility.isNullOrBlank(null));
        }

        @Test
        @DisplayName("Given empty string when isNullOrBlank then return true")
        void givenEmptyStringWhenIsNullOrBlankThenReturnTrue() {
            assertTrue(StringUtility.isNullOrBlank(""));
        }

        @Test
        @DisplayName("Given blank string when isNullOrBlank then return true")
        void givenBlankStringWhenIsNullOrBlankThenReturnTrue() {
            assertTrue(StringUtility.isNullOrBlank("   "));
        }

        @Test
        @DisplayName("Given non-blank string when isNullOrBlank then return false")
        void givenNonBlankStringWhenIsNullOrBlankThenReturnFalse() {
            assertFalse(StringUtility.isNullOrBlank("hello"));
        }
    }

    // ── capitalizeFirstLetter ────────────────────────────────────

    @Nested
    @DisplayName("capitalizeFirstLetter")
    class CapitalizeFirstLetterTests {

        @Test
        @DisplayName("Given null when capitalizeFirstLetter then return null")
        void givenNullWhenCapitalizeFirstLetterThenReturnNull() {
            assertNull(StringUtility.capitalizeFirstLetter(null));
        }

        @Test
        @DisplayName("Given empty string when capitalizeFirstLetter then return empty string")
        void givenEmptyStringWhenCapitalizeFirstLetterThenReturnEmptyString() {
            assertEquals("", StringUtility.capitalizeFirstLetter(""));
        }

        @Test
        @DisplayName("Given lowercase first letter when capitalizeFirstLetter then return capitalized")
        void givenLowercaseFirstLetterWhenCapitalizeFirstLetterThenReturnCapitalized() {
            assertEquals("Hello", StringUtility.capitalizeFirstLetter("hello"));
        }

        @Test
        @DisplayName("Given already uppercase first letter when capitalizeFirstLetter then return unchanged")
        void givenAlreadyUppercaseFirstLetterWhenCapitalizeFirstLetterThenReturnUnchanged() {
            assertEquals("Hello", StringUtility.capitalizeFirstLetter("Hello"));
        }

        @Test
        @DisplayName("Given single lowercase char when capitalizeFirstLetter then return uppercase")
        void givenSingleLowercaseCharWhenCapitalizeFirstLetterThenReturnUppercase() {
            assertEquals("A", StringUtility.capitalizeFirstLetter("a"));
        }
    }

    // ── decapitalizeFirstLetter ──────────────────────────────────

    @Nested
    @DisplayName("decapitalizeFirstLetter")
    class DecapitalizeFirstLetterTests {

        @Test
        @DisplayName("Given null when decapitalizeFirstLetter then return null")
        void givenNullWhenDecapitalizeFirstLetterThenReturnNull() {
            assertNull(StringUtility.decapitalizeFirstLetter(null));
        }

        @Test
        @DisplayName("Given empty string when decapitalizeFirstLetter then return empty string")
        void givenEmptyStringWhenDecapitalizeFirstLetterThenReturnEmptyString() {
            assertEquals("", StringUtility.decapitalizeFirstLetter(""));
        }

        @Test
        @DisplayName("Given uppercase first letter when decapitalizeFirstLetter then return decapitalized")
        void givenUppercaseFirstLetterWhenDecapitalizeFirstLetterThenReturnDecapitalized() {
            assertEquals("hELLO", StringUtility.decapitalizeFirstLetter("HELLO"));
        }

        @Test
        @DisplayName("Given already lowercase first letter when decapitalizeFirstLetter then return unchanged")
        void givenAlreadyLowercaseFirstLetterWhenDecapitalizeFirstLetterThenReturnUnchanged() {
            assertEquals("hello", StringUtility.decapitalizeFirstLetter("hello"));
        }

        @Test
        @DisplayName("Given single uppercase char when decapitalizeFirstLetter then return lowercase")
        void givenSingleUppercaseCharWhenDecapitalizeFirstLetterThenReturnLowercase() {
            assertEquals("a", StringUtility.decapitalizeFirstLetter("A"));
        }
    }

    // ── capitalizeFirstLetterWithUnderscore ──────────────────────

    @Nested
    @DisplayName("capitalizeFirstLetterWithUnderscore")
    class CapitalizeFirstLetterWithUnderscoreTests {

        @Test
        @DisplayName("Given null when capitalizeFirstLetterWithUnderscore then return null")
        void givenNullWhenCapitalizeFirstLetterWithUnderscoreThenReturnNull() {
            assertNull(StringUtility.capitalizeFirstLetterWithUnderscore(null));
        }

        @Test
        @DisplayName("Given empty string when capitalizeFirstLetterWithUnderscore then return empty string")
        void givenEmptyStringWhenCapitalizeFirstLetterWithUnderscoreThenReturnEmptyString() {
            assertEquals("", StringUtility.capitalizeFirstLetterWithUnderscore(""));
        }

        @Test
        @DisplayName("Given lowercase first letter when capitalizeFirstLetterWithUnderscore then return capitalized")
        void givenLowercaseFirstLetterWhenCapitalizeFirstLetterWithUnderscoreThenReturnCapitalized() {
            assertEquals("Hello", StringUtility.capitalizeFirstLetterWithUnderscore("hello"));
        }

        @Test
        @DisplayName("Given already uppercase first letter when capitalizeFirstLetterWithUnderscore then return unchanged")
        void givenAlreadyUppercaseFirstLetterWhenCapitalizeFirstLetterWithUnderscoreThenReturnUnchanged() {
            assertEquals("Hello", StringUtility.capitalizeFirstLetterWithUnderscore("Hello"));
        }
    }

    // ── collapse(String...) ──────────────────────────────────────

    @Nested
    @DisplayName("collapse(String...)")
    class CollapseVarargsTests {

        @Test
        @DisplayName("Given first string non-empty when collapse then return first string")
        void givenFirstStringNonEmptyWhenCollapseThenReturnFirstString() {
            assertEquals("first", StringUtility.collapse("first", "second", "third"));
        }

        @Test
        @DisplayName("Given first null second non-empty when collapse then return second string")
        void givenFirstNullSecondNonEmptyWhenCollapseThenReturnSecondString() {
            assertEquals("second", StringUtility.collapse(null, "second", "third"));
        }

        @Test
        @DisplayName("Given first empty second non-empty when collapse then return second string")
        void givenFirstEmptySecondNonEmptyWhenCollapseThenReturnSecondString() {
            assertEquals("second", StringUtility.collapse("", "second", "third"));
        }

        @Test
        @DisplayName("Given all null when collapse then return null")
        void givenAllNullWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse((String) null, null, null));
        }

        @Test
        @DisplayName("Given all empty when collapse then return null")
        void givenAllEmptyWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse("", "", ""));
        }

        @Test
        @DisplayName("Given mixed null and empty when collapse then return null")
        void givenMixedNullAndEmptyWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse((String) null, "", null));
        }

        @Test
        @DisplayName("Given single non-empty when collapse then return it")
        void givenSingleNonEmptyWhenCollapseThenReturnIt() {
            assertEquals("only", StringUtility.collapse("only"));
        }

        @Test
        @DisplayName("Given no arguments when collapse then return null")
        void givenNoArgumentsWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse(new String[0]));
        }
    }

    // ── collapse(Supplier<String>...) ────────────────────────────

    @Nested
    @DisplayName("collapse(Supplier<String>...)")
    class CollapseSuppliersTests {

        @Test
        @DisplayName("Given first supplier non-empty when collapse then return first value")
        void givenFirstSupplierNonEmptyWhenCollapseThenReturnFirstValue() {
            assertEquals("first", StringUtility.collapse(
                    () -> "first", () -> "second", () -> "third"));
        }

        @Test
        @DisplayName("Given first null second non-empty when collapse then return second value")
        void givenFirstNullSecondNonEmptyWhenCollapseThenReturnSecondValue() {
            assertEquals("second", StringUtility.collapse(
                    (Supplier<String>) () -> null, () -> "second", () -> "third"));
        }

        @Test
        @DisplayName("Given first empty second non-empty when collapse then return second value")
        void givenFirstEmptySecondNonEmptyWhenCollapseThenReturnSecondValue() {
            assertEquals("second", StringUtility.collapse(
                    () -> "", () -> "second", () -> "third"));
        }

        @Test
        @DisplayName("Given all suppliers return null when collapse then return null")
        void givenAllSuppliersReturnNullWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse((Supplier<String>) () -> null, (Supplier<String>) () -> null));
        }

        @Test
        @DisplayName("Given all suppliers return empty when collapse then return null")
        void givenAllSuppliersReturnEmptyWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse(() -> "", () -> ""));
        }

        @Test
        @DisplayName("Given mixed null and empty suppliers when collapse then return null")
        void givenMixedNullAndEmptySuppliersWhenCollapseThenReturnNull() {
            assertNull(StringUtility.collapse(() -> null, () -> "", () -> null));
        }

        @Test
        @DisplayName("Given no suppliers when collapse then return null")
        void givenNoSuppliersWhenCollapseThenReturnNull() {
            @SuppressWarnings("unchecked")
            Supplier<String>[] empty = new Supplier[0];
            assertNull(StringUtility.collapse(empty));
        }

        @Test
        @DisplayName("Given lazy evaluation when collapse then only evaluate until first non-empty")
        void givenLazyEvaluationWhenCollapseThenOnlyEvaluateUntilFirstNonEmpty() {
            StringUtility.collapse(
                    () -> "first",
                    () -> { throw new RuntimeException("should not be evaluated"); });
            // 不应抛出异常，因为第二个 supplier 不会被求值
        }
    }
}
