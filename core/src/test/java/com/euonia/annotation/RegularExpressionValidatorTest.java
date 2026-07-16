package com.euonia.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegularExpressionValidator")
class RegularExpressionValidatorTest {

    private final RegularExpressionValidator validator = new RegularExpressionValidator();

    private static class Samples {
        @RegularExpression("\\d+")
        private String digits;

        @RegularExpression(value = "[a-z]+", message = "custom regex")
        private String customMessage;
    }

    @Test
    @DisplayName("Given matching string when validating then validation succeeds")
    void givenMatchingStringWhenValidatingThenSucceed() throws NoSuchFieldException {
        RegularExpression annotation = Samples.class.getDeclaredField("digits").getAnnotation(RegularExpression.class);

        Validator.Result result = validator.validate(annotation, "12345");

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given non-matching string when validating then validation fails with default message")
    void givenNonMatchingStringWhenValidatingThenFailWithDefaultMessage() throws NoSuchFieldException {
        RegularExpression annotation = Samples.class.getDeclaredField("digits").getAnnotation(RegularExpression.class);

        Validator.Result result = validator.validate(annotation, "abc");

        assertFalse(result.result());
        assertEquals("Value must match the regular expression: \\d+", result.message());
    }

    @Test
    @DisplayName("Given non-matching string and custom message when validating then custom message is returned")
    void givenNonMatchingStringAndCustomMessageWhenValidatingThenReturnCustomMessage() throws NoSuchFieldException {
        RegularExpression annotation = Samples.class.getDeclaredField("customMessage").getAnnotation(RegularExpression.class);

        Validator.Result result = validator.validate(annotation, "123");

        assertFalse(result.result());
        assertEquals("custom regex", result.message());
    }

    @Test
    @DisplayName("Given non-string value when validating then validation succeeds")
    void givenNonStringValueWhenValidatingThenSucceed() throws NoSuchFieldException {
        RegularExpression annotation = Samples.class.getDeclaredField("digits").getAnnotation(RegularExpression.class);

        Validator.Result result = validator.validate(annotation, 42);

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given null value when validating then validation succeeds")
    void givenNullValueWhenValidatingThenSucceed() throws NoSuchFieldException {
        RegularExpression annotation = Samples.class.getDeclaredField("digits").getAnnotation(RegularExpression.class);

        Validator.Result result = validator.validate(annotation, null);

        assertTrue(result.result());
        assertNull(result.message());
    }
}
