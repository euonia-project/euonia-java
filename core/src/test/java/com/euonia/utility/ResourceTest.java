package com.euonia.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Resource")
class ResourceTest {

    @Test
    @DisplayName("Given valid resource key when getString then returns non-null value")
    void givenValidResourceKeyWhenGetStringThenReturnsNonNullValue() {
        String result = Resource.getString("core", "ArgumentNullException.Message1", "testParam");

        assertNotNull(result);
        assertTrue(result.contains("testParam"));
    }

    @Test
    @DisplayName("Given valid resource key without args when getString then returns message template")
    void givenValidResourceKeyWithoutArgsWhenGetStringThenReturnsMessageTemplate() {
        String result = Resource.getString("core", "ArgumentNullException.Message1");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Given reflection resource key when getString then returns non-null value")
    void givenReflectionResourceKeyWhenGetStringThenReturnsNonNullValue() {
        String result = Resource.getString("reflection", "AmbiguousMethodException.Message");

        assertNotNull(result);
    }
}
