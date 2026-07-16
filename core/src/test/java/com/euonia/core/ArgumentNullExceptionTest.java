package com.euonia.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArgumentNullException")
class ArgumentNullExceptionTest {

    @Test
    @DisplayName("Given constructor with message when creating then message is preserved")
    void givenConstructorWithMessageWhenCreatingThenMessageIsPreserved() {
        ArgumentNullException exception = new ArgumentNullException("test message");

        assertEquals("test message", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception);
    }

    @Test
    @DisplayName("Given null object when throwIfNull with param then exception is thrown")
    void givenNullObjectWhenThrowIfNullWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNull(null, "param"));
    }

    @Test
    @DisplayName("Given non-null object when throwIfNull with param then no exception is thrown")
    void givenNonNullObjectWhenThrowIfNullWithParamThenNoException() {
        assertDoesNotThrow(() -> ArgumentNullException.throwIfNull("value", "param"));
    }

    @Test
    @DisplayName("Given null object when throwIfNull with supplier then exception is thrown")
    void givenNullObjectWhenThrowIfNullWithSupplierThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNull(null, () -> "custom"));
    }

    @Test
    @DisplayName("Given null string when throwIfNullOrEmpty with param then exception is thrown")
    void givenNullStringWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty((String) null, "param"));
    }

    @Test
    @DisplayName("Given empty string when throwIfNullOrEmpty with param then exception is thrown")
    void givenEmptyStringWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty("", "param"));
    }

    @Test
    @DisplayName("Given non-empty string when throwIfNullOrEmpty with param then no exception")
    void givenNonEmptyStringWhenThrowIfNullOrEmptyWithParamThenNoException() {
        assertDoesNotThrow(() -> ArgumentNullException.throwIfNullOrEmpty("value", "param"));
    }

    @Test
    @DisplayName("Given null array when throwIfNullOrEmpty with param then exception is thrown")
    void givenNullArrayWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty((String[]) null, "param"));
    }

    @Test
    @DisplayName("Given empty array when throwIfNullOrEmpty with param then exception is thrown")
    void givenEmptyArrayWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty(new String[0], "param"));
    }

    @Test
    @DisplayName("Given non-empty array when throwIfNullOrEmpty with param then no exception")
    void givenNonEmptyArrayWhenThrowIfNullOrEmptyWithParamThenNoException() {
        assertDoesNotThrow(() -> ArgumentNullException.throwIfNullOrEmpty(new String[]{"a"}, "param"));
    }

    @Test
    @DisplayName("Given null list when throwIfNullOrEmpty with param then exception is thrown")
    void givenNullListWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty((List<String>) null, "param"));
    }

    @Test
    @DisplayName("Given empty list when throwIfNullOrEmpty with param then exception is thrown")
    void givenEmptyListWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty(new ArrayList<>(), "param"));
    }

    @Test
    @DisplayName("Given null collection when throwIfNullOrEmpty with param then exception is thrown")
    void givenNullCollectionWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty((Collection<String>) null, "param"));
    }

    @Test
    @DisplayName("Given empty collection when throwIfNullOrEmpty with param then exception is thrown")
    void givenEmptyCollectionWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty(new ArrayList<>(), "param"));
    }

    @Test
    @DisplayName("Given null map when throwIfNullOrEmpty with param then exception is thrown")
    void givenNullMapWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty((Map<String, String>) null, "param"));
    }

    @Test
    @DisplayName("Given empty map when throwIfNullOrEmpty with param then exception is thrown")
    void givenEmptyMapWhenThrowIfNullOrEmptyWithParamThenExceptionIsThrown() {
        assertThrows(ArgumentNullException.class, () -> ArgumentNullException.throwIfNullOrEmpty(new HashMap<>(), "param"));
    }

    @Test
    @DisplayName("Given null string when throwIfNullOrEmpty with supplier then exception with custom message is thrown")
    void givenNullStringWhenThrowIfNullOrEmptyWithSupplierThenExceptionWithCustomMessageIsThrown() {
        ArgumentNullException exception = assertThrows(ArgumentNullException.class,
                () -> ArgumentNullException.throwIfNullOrEmpty((String) null, () -> "custom null msg"));
        assertEquals("custom null msg", exception.getMessage());
    }

    @Test
    @DisplayName("Given null list when throwIfNullOrEmpty with supplier then exception with custom message is thrown")
    void givenNullListWhenThrowIfNullOrEmptyWithSupplierThenExceptionWithCustomMessageIsThrown() {
        ArgumentNullException exception = assertThrows(ArgumentNullException.class,
                () -> ArgumentNullException.throwIfNullOrEmpty((List<String>) null, () -> "custom list msg"));
        assertEquals("custom list msg", exception.getMessage());
    }

    @Test
    @DisplayName("Given null array when throwIfNullOrEmpty with supplier then exception with custom message is thrown")
    void givenNullArrayWhenThrowIfNullOrEmptyWithSupplierThenExceptionWithCustomMessageIsThrown() {
        ArgumentNullException exception = assertThrows(ArgumentNullException.class,
                () -> ArgumentNullException.throwIfNullOrEmpty((String[]) null, () -> "custom array msg"));
        assertEquals("custom array msg", exception.getMessage());
    }

    @Test
    @DisplayName("Given null map when throwIfNullOrEmpty with supplier then exception with custom message is thrown")
    void givenNullMapWhenThrowIfNullOrEmptyWithSupplierThenExceptionWithCustomMessageIsThrown() {
        ArgumentNullException exception = assertThrows(ArgumentNullException.class,
                () -> ArgumentNullException.throwIfNullOrEmpty((Map<String, String>) null, () -> "custom map msg"));
        assertEquals("custom map msg", exception.getMessage());
    }

    @Test
    @DisplayName("Given null collection when throwIfNullOrEmpty with supplier then exception with custom message is thrown")
    void givenNullCollectionWhenThrowIfNullOrEmptyWithSupplierThenExceptionWithCustomMessageIsThrown() {
        ArgumentNullException exception = assertThrows(ArgumentNullException.class,
                () -> ArgumentNullException.throwIfNullOrEmpty((Collection<String>) null, () -> "custom col msg"));
        assertEquals("custom col msg", exception.getMessage());
    }
}
