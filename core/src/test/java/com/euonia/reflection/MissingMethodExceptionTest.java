package com.euonia.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MissingMethodException")
class MissingMethodExceptionTest {

    @Test
    @DisplayName("Given typeName and methodName constructor when creating then properties are preserved")
    void givenTypeNameAndMethodNameConstructorWhenCreatingThenPropertiesArePreserved() {
        MissingMethodException exception = new MissingMethodException("MyClass", "myMethod");

        assertEquals("MyClass", exception.getTypeName());
        assertEquals("myMethod", exception.getMethodName());
        assertNotNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("MissingMethodException should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        assertInstanceOf(RuntimeException.class, new MissingMethodException("TestClass", "test"));
    }
}
