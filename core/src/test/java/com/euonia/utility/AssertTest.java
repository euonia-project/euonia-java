package com.euonia.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Assert")
class AssertTest {

    @Test
    @DisplayName("Given non-null object when notNull then no exception")
    void givenNonNullObjectWhenNotNullThenNoException() {
        assertDoesNotThrow(() -> Assert.notNull("value", "should not throw"));
    }

    @Test
    @DisplayName("Given null object when notNull then throws IllegalArgumentException")
    void givenNullObjectWhenNotNullThenThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Assert.notNull(null, "must not be null"));
        assertEquals("must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Given non-null object when notNull with supplier then no exception")
    void givenNonNullObjectWhenNotNullWithSupplierThenNoException() {
        assertDoesNotThrow(() -> Assert.notNull("value", () -> "should not throw"));
    }

    @Test
    @DisplayName("Given null object when notNull with supplier then throws with supplier message")
    void givenNullObjectWhenNotNullWithSupplierThenThrowsWithSupplierMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Assert.notNull(null, () -> "supplier message"));
        assertEquals("supplier message", exception.getMessage());
    }

    @Test
    @DisplayName("Given non-empty string when notEmpty then no exception")
    void givenNonEmptyStringWhenNotEmptyThenNoException() {
        assertDoesNotThrow(() -> Assert.notEmpty("hello", "should not throw"));
    }

    @Test
    @DisplayName("Given null string when notEmpty then throws IllegalArgumentException")
    void givenNullStringWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty((String) null, "string must not be empty"));
    }

    @Test
    @DisplayName("Given empty string when notEmpty then throws IllegalArgumentException")
    void givenEmptyStringWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty("", "string must not be empty"));
    }

    @Test
    @DisplayName("Given blank string when notEmpty then throws IllegalArgumentException")
    void givenBlankStringWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty("   ", "string must not be blank"));
    }

    @Test
    @DisplayName("Given non-empty list when notEmpty then no exception")
    void givenNonEmptyListWhenNotEmptyThenNoException() {
        List<String> list = Arrays.asList("a", "b");
        assertDoesNotThrow(() -> Assert.notEmpty(list, "should not throw"));
    }

    @Test
    @DisplayName("Given null list when notEmpty then throws IllegalArgumentException")
    void givenNullListWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty((List<?>) null, "list must not be empty"));
    }

    @Test
    @DisplayName("Given empty list when notEmpty then throws IllegalArgumentException")
    void givenEmptyListWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty(new ArrayList<>(), "list must not be empty"));
    }

    @Test
    @DisplayName("Given non-empty array when notEmpty then no exception")
    void givenNonEmptyArrayWhenNotEmptyThenNoException() {
        assertDoesNotThrow(() -> Assert.notEmpty(new String[]{"a", "b"}, "should not throw"));
    }

    @Test
    @DisplayName("Given null array when notEmpty then throws IllegalArgumentException")
    void givenNullArrayWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty((Object[]) null, "array must not be empty"));
    }

    @Test
    @DisplayName("Given empty array when notEmpty then throws IllegalArgumentException")
    void givenEmptyArrayWhenNotEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notEmpty(new String[0], "array must not be empty"));
    }

    @Test
    @DisplayName("Given list with no matching element when notContains then no exception")
    void givenListWithNoMatchingElementWhenNotContainsThenNoException() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertDoesNotThrow(() -> Assert.notContains(list, s -> s.equals("d"), "should not throw"));
    }

    @Test
    @DisplayName("Given list with matching element when notContains then throws IllegalArgumentException")
    void givenListWithMatchingElementWhenNotContainsThenThrowsIllegalArgumentException() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notContains(list, s -> s.equals("b"), "contains forbidden"));
    }

    @Test
    @DisplayName("Given null list when notContains then no exception")
    void givenNullListWhenNotContainsThenNoException() {
        assertDoesNotThrow(() -> Assert.notContains((List<String>) null, s -> s.equals("a"), "should not throw"));
    }

    @Test
    @DisplayName("Given array with no matching element when notContains then no exception")
    void givenArrayWithNoMatchingElementWhenNotContainsThenNoException() {
        String[] array = {"a", "b", "c"};
        assertDoesNotThrow(() -> Assert.notContains(array, s -> s.equals("d"), "should not throw"));
    }

    @Test
    @DisplayName("Given array with matching element when notContains then throws IllegalArgumentException")
    void givenArrayWithMatchingElementWhenNotContainsThenThrowsIllegalArgumentException() {
        String[] array = {"a", "b", "c"};
        assertThrows(IllegalArgumentException.class,
                () -> Assert.notContains(array, s -> s.equals("b"), "contains forbidden"));
    }

    @Test
    @DisplayName("Given null array when notContains then no exception")
    void givenNullArrayWhenNotContainsThenNoException() {
        assertDoesNotThrow(() -> Assert.notContains((String[]) null, s -> s.equals("a"), "should not throw"));
    }
}
