package com.euonia.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AmbiguousMethodException")
class AmbiguousMethodExceptionTest {

    @Test
    @DisplayName("Given no-arg constructor when creating then message is not null")
    void givenNoArgConstructorWhenCreatingThenMessageIsNotNull() {
        AmbiguousMethodException exception = new AmbiguousMethodException();

        assertNotNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given message constructor when creating then message is preserved")
    void givenMessageConstructorWhenCreatingThenMessageIsPreserved() {
        AmbiguousMethodException exception = new AmbiguousMethodException("ambiguous method");

        assertEquals("ambiguous method", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given message and cause constructor when creating then both are preserved")
    void givenMessageAndCauseConstructorWhenCreatingThenBothArePreserved() {
        Throwable cause = new RuntimeException("root cause");
        AmbiguousMethodException exception = new AmbiguousMethodException("ambiguous method", cause);

        assertEquals("ambiguous method", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("AmbiguousMethodException should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        assertInstanceOf(RuntimeException.class, new AmbiguousMethodException("test"));
    }
}
