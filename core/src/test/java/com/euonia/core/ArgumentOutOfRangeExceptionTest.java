package com.euonia.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArgumentOutOfRangeException")
class ArgumentOutOfRangeExceptionTest {

    @Test
    @DisplayName("Given constructor with message when creating then message is preserved")
    void givenConstructorWithMessageWhenCreatingThenMessageIsPreserved() {
        ArgumentOutOfRangeException exception = new ArgumentOutOfRangeException("out of range");

        assertEquals("out of range", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception);
    }

    @Test
    @DisplayName("Given equal values when throwIfEquals then exception is thrown")
    void givenEqualValuesWhenThrowIfEqualsThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfEquals(5, 5, () -> "equals"));
    }

    @Test
    @DisplayName("Given not equal values when throwIfEquals then no exception")
    void givenNotEqualValuesWhenThrowIfEqualsThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfEquals(5, 3, () -> "equals"));
    }

    @Test
    @DisplayName("Given greater value when throwIfGreaterThan then exception is thrown")
    void givenGreaterValueWhenThrowIfGreaterThanThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfGreaterThan(10, 5, () -> "greater"));
    }

    @Test
    @DisplayName("Given equal value when throwIfGreaterThan then no exception")
    void givenEqualValueWhenThrowIfGreaterThanThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfGreaterThan(5, 5, () -> "greater"));
    }

    @Test
    @DisplayName("Given less value when throwIfGreaterThan then no exception")
    void givenLessValueWhenThrowIfGreaterThanThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfGreaterThan(3, 5, () -> "greater"));
    }

    @Test
    @DisplayName("Given greater or equal value when throwIfGreaterThanOrEqual then exception is thrown")
    void givenGreaterOrEqualValueWhenThrowIfGreaterThanOrEqualThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfGreaterThanOrEqual(5, 5, () -> "gte"));
    }

    @Test
    @DisplayName("Given less value when throwIfGreaterThanOrEqual then no exception")
    void givenLessValueWhenThrowIfGreaterThanOrEqualThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfGreaterThanOrEqual(3, 5, () -> "gte"));
    }

    @Test
    @DisplayName("Given less value when throwIfLessThan then exception is thrown")
    void givenLessValueWhenThrowIfLessThanThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfLessThan(3, 5, () -> "less"));
    }

    @Test
    @DisplayName("Given equal value when throwIfLessThan then no exception")
    void givenEqualValueWhenThrowIfLessThanThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfLessThan(5, 5, () -> "less"));
    }

    @Test
    @DisplayName("Given less or equal value when throwIfLessThanOrEqual then exception is thrown")
    void givenLessOrEqualValueWhenThrowIfLessThanOrEqualThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfLessThanOrEqual(5, 5, () -> "lte"));
    }

    @Test
    @DisplayName("Given greater value when throwIfLessThanOrEqual then no exception")
    void givenGreaterValueWhenThrowIfLessThanOrEqualThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfLessThanOrEqual(10, 5, () -> "lte"));
    }

    @Test
    @DisplayName("Given value not in range when throwIfNotInRange then exception is thrown")
    void givenValueNotInRangeWhenThrowIfNotInRangeThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfNotInRange(10, 1, 5, () -> "not in range"));
    }

    @Test
    @DisplayName("Given value in range when throwIfNotInRange then no exception")
    void givenValueInRangeWhenThrowIfNotInRangeThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNotInRange(3, 1, 5, () -> "in range"));
    }

    @Test
    @DisplayName("Given value at min boundary when throwIfNotInRange then no exception")
    void givenValueAtMinBoundaryWhenThrowIfNotInRangeThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNotInRange(1, 1, 5, () -> "at min"));
    }

    @Test
    @DisplayName("Given value at max boundary when throwIfNotInRange then no exception")
    void givenValueAtMaxBoundaryWhenThrowIfNotInRangeThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNotInRange(5, 1, 5, () -> "at max"));
    }

    @Test
    @DisplayName("Given value in range when throwIfInRange then exception is thrown")
    void givenValueInRangeWhenThrowIfInRangeThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfInRange(3, 1, 5, () -> "in range"));
    }

    @Test
    @DisplayName("Given value not in range when throwIfInRange then no exception")
    void givenValueNotInRangeWhenThrowIfInRangeThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfInRange(10, 1, 5, () -> "not in range"));
    }

    @Test
    @DisplayName("Given Range object when throwIfNotInRange then delegates to min max")
    void givenRangeObjectWhenThrowIfNotInRangeThenDelegates() {
        Range<Integer> range = Range.of(1, 10);
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfNotInRange(20, range, () -> "not in range"));
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNotInRange(5, range, () -> "in range"));
    }

    @Test
    @DisplayName("Given Range object when throwIfInRange then delegates to min max")
    void givenRangeObjectWhenThrowIfInRangeThenDelegates() {
        Range<Integer> range = Range.of(1, 10);
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfInRange(5, range, () -> "in range"));
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfInRange(20, range, () -> "not in range"));
    }

    @Test
    @DisplayName("Given negative number when throwIfNegative then exception is thrown")
    void givenNegativeNumberWhenThrowIfNegativeThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfNegative(-1, () -> "negative"));
    }

    @Test
    @DisplayName("Given zero when throwIfNegative then no exception")
    void givenZeroWhenThrowIfNegativeThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNegative(0, () -> "negative"));
    }

    @Test
    @DisplayName("Given positive when throwIfNegative then no exception")
    void givenPositiveWhenThrowIfNegativeThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNegative(1, () -> "negative"));
    }

    @Test
    @DisplayName("Given negative number when throwIfNegativeOrZero then exception is thrown")
    void givenNegativeNumberWhenThrowIfNegativeOrZeroThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfNegativeOrZero(-1, () -> "neg or zero"));
    }

    @Test
    @DisplayName("Given zero when throwIfNegativeOrZero then exception is thrown")
    void givenZeroWhenThrowIfNegativeOrZeroThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfNegativeOrZero(0, () -> "neg or zero"));
    }

    @Test
    @DisplayName("Given positive when throwIfNegativeOrZero then no exception")
    void givenPositiveWhenThrowIfNegativeOrZeroThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfNegativeOrZero(1, () -> "neg or zero"));
    }

    @Test
    @DisplayName("Given zero when throwIfZero then exception is thrown")
    void givenZeroWhenThrowIfZeroThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfZero(0, () -> "zero"));
    }

    @Test
    @DisplayName("Given non-zero when throwIfZero then no exception")
    void givenNonZeroWhenThrowIfZeroThenNoException() {
        assertDoesNotThrow(() -> ArgumentOutOfRangeException.throwIfZero(5, () -> "zero"));
    }

    @Test
    @DisplayName("Given equal values when throwIfEquals with param then exception is thrown")
    void givenEqualValuesWhenThrowIfEqualsWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfEquals(5, 5, "param"));
    }

    @Test
    @DisplayName("Given greater value when throwIfGreaterThan with param then exception is thrown")
    void givenGreaterValueWhenThrowIfGreaterThanWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfGreaterThan(10, 5, "param"));
    }

    @Test
    @DisplayName("Given negative number when throwIfNegative with param then exception is thrown")
    void givenNegativeNumberWhenThrowIfNegativeWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfNegative(-5.5, "param"));
    }

    @Test
    @DisplayName("Given zero when throwIfZero with param then exception is thrown")
    void givenZeroWhenThrowIfZeroWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentOutOfRangeException.class,
                () -> ArgumentOutOfRangeException.throwIfZero(0, "param"));
    }
}
