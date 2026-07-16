package com.euonia.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringLengthValidator")
class StringLengthValidatorTest {

    private final StringLengthValidator validator = new StringLengthValidator();

    private static class Samples {
        @StringLength(min = 2, max = 5)
        private String constrained;

        @StringLength(min = 0, max = 10, message = "custom length")
        private String customMessage;
    }

    @Test
    @DisplayName("Given string within range when validating then validation succeeds")
    void givenStringWithinRangeWhenValidatingThenSucceed() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, "abc");

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given string too short when validating then validation fails with default message")
    void givenStringTooShortWhenValidatingThenFailWithDefaultMessage() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, "a");

        assertFalse(result.result());
        assertEquals("Value must be between 2 and 5 characters long", result.message());
    }

    @Test
    @DisplayName("Given string too long when validating then validation fails with default message")
    void givenStringTooLongWhenValidatingThenFailWithDefaultMessage() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, "abcdef");

        assertFalse(result.result());
        assertEquals("Value must be between 2 and 5 characters long", result.message());
    }

    @Test
    @DisplayName("Given string at exact min boundary when validating then validation succeeds")
    void givenStringAtExactMinBoundaryWhenValidatingThenSucceed() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, "ab");

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given string at exact max boundary when validating then validation succeeds")
    void givenStringAtExactMaxBoundaryWhenValidatingThenSucceed() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, "abcde");

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given out-of-range string and custom message when validating then custom message is returned")
    void givenOutOfRangeStringAndCustomMessageWhenValidatingThenReturnCustomMessage() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("customMessage").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, "12345678901");

        assertFalse(result.result());
        assertEquals("custom length", result.message());
    }

    @Test
    @DisplayName("Given non-string value when validating then validation succeeds")
    void givenNonStringValueWhenValidatingThenSucceed() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, 42);

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given null value when validating then validation succeeds")
    void givenNullValueWhenValidatingThenSucceed() throws NoSuchFieldException {
        StringLength annotation = Samples.class.getDeclaredField("constrained").getAnnotation(StringLength.class);

        Validator.Result result = validator.validate(annotation, null);

        assertTrue(result.result());
        assertNull(result.message());
    }
}
