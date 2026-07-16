package com.euonia.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Range")
class RangeTest {

    @Test
    @DisplayName("Given min less than max when creating Range.of then returns valid range")
    void givenMinLessThanMaxWhenCreatingRangeThenReturnsValidRange() {
        Range<Integer> range = Range.of(1, 10);

        assertEquals(1, range.min());
        assertEquals(10, range.max());
    }

    @Test
    @DisplayName("Given min equals max when creating Range.of then returns valid range")
    void givenMinEqualsMaxWhenCreatingRangeThenReturnsValidRange() {
        Range<Integer> range = Range.of(5, 5);

        assertEquals(5, range.min());
        assertEquals(5, range.max());
    }

    @Test
    @DisplayName("Given min greater than max when creating Range.of then throws IllegalArgumentException")
    void givenMinGreaterThanMaxWhenCreatingRangeThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Range.of(10, 1));
    }

    @Test
    @DisplayName("Given value within range when checking then returns true")
    void givenValueWithinRangeWhenCheckingThenReturnsTrue() {
        Range<Integer> range = Range.of(1, 10);

        assertTrue(range.check(1));
        assertTrue(range.check(5));
        assertTrue(range.check(10));
    }

    @Test
    @DisplayName("Given value below range when checking then returns false")
    void givenValueBelowRangeWhenCheckingThenReturnsFalse() {
        Range<Integer> range = Range.of(1, 10);

        assertFalse(range.check(0));
    }

    @Test
    @DisplayName("Given value above range when checking then returns false")
    void givenValueAboveRangeWhenCheckingThenReturnsFalse() {
        Range<Integer> range = Range.of(1, 10);

        assertFalse(range.check(11));
    }
}
