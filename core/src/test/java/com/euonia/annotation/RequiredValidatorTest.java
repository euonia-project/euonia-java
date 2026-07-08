package com.euonia.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequiredValidator")
class RequiredValidatorTest {

    private final RequiredValidator validator = new RequiredValidator();

    private static class Samples {
        @Required
        private String defaultRequired;

        @Required(allowEmpty = true)
        private String allowEmpty;

        @Required(message = "custom required")
        private String customMessage;
    }

    @Test
    @DisplayName("Given null value when validating required field then validation fails with default message")
    void givenNullValueWhenValidatingThenFailWithDefaultMessage() throws NoSuchFieldException {
        Required annotation = Samples.class.getDeclaredField("defaultRequired").getAnnotation(Required.class);

        Validator.Result result = validator.validate(annotation, null);

        assertFalse(result.result());
        assertEquals("Value is required", result.message());
    }

    @Test
    @DisplayName("Given empty string and allowEmpty false when validating then validation fails")
    void givenEmptyStringAndAllowEmptyFalseWhenValidatingThenFail() throws NoSuchFieldException {
        Required annotation = Samples.class.getDeclaredField("defaultRequired").getAnnotation(Required.class);

        Validator.Result result = validator.validate(annotation, "");

        assertFalse(result.result());
        assertEquals("Value must not be empty", result.message());
    }

    @Test
    @DisplayName("Given empty string and allowEmpty true when validating then validation succeeds")
    void givenEmptyStringAndAllowEmptyTrueWhenValidatingThenSucceed() throws NoSuchFieldException {
        Required annotation = Samples.class.getDeclaredField("allowEmpty").getAnnotation(Required.class);

        Validator.Result result = validator.validate(annotation, "");

        assertTrue(result.result());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Given null value and custom message when validating then custom message is returned")
    void givenNullValueAndCustomMessageWhenValidatingThenReturnCustomMessage() throws NoSuchFieldException {
        Required annotation = Samples.class.getDeclaredField("customMessage").getAnnotation(Required.class);

        Validator.Result result = validator.validate(annotation, null);

        assertFalse(result.result());
        assertEquals("custom required", result.message());
    }

    @Test
    @DisplayName("Given non-empty value when validating then validation succeeds")
    void givenNonEmptyValueWhenValidatingThenSucceed() throws NoSuchFieldException {
        Required annotation = Samples.class.getDeclaredField("defaultRequired").getAnnotation(Required.class);

        Validator.Result result = validator.validate(annotation, "abc");

        assertTrue(result.result());
        assertNull(result.message());
    }
}

