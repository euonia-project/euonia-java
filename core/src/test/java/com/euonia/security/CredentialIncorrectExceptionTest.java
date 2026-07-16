package com.euonia.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CredentialIncorrectException")
class CredentialIncorrectExceptionTest {

    @Test
    @DisplayName("Given credential and optional message cause when constructing then properties are preserved")
    void givenConstructorsWhenCreatingThenPropertiesArePreserved() {
        Throwable cause = new IllegalArgumentException("incorrect");

        CredentialIncorrectException oneArg = new CredentialIncorrectException("cred1");
        CredentialIncorrectException twoArg = new CredentialIncorrectException("cred2", "msg");
        CredentialIncorrectException threeArg = new CredentialIncorrectException("cred3", "msg2", cause);

        assertEquals("cred1", oneArg.getCredential());
        assertNull(oneArg.getMessage());
        assertEquals("cred2", twoArg.getCredential());
        assertEquals("msg", twoArg.getMessage());
        assertEquals("cred3", threeArg.getCredential());
        assertEquals("msg2", threeArg.getMessage());
        assertSame(cause, threeArg.getCause());
    }

    @Test
    @DisplayName("Given details map when setting values then values are stored and retrievable")
    void givenDetailsWhenSettingThenValuesAreStored() {
        CredentialIncorrectException exception = new CredentialIncorrectException("credential");

        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails().isEmpty());
        assertNull(exception.get("missing"));

        exception.set("k", "v");
        assertEquals("v", exception.get("k"));
        assertEquals(1, exception.getDetails().size());

        CredentialException chained = exception.with("k2", "v2");
        assertSame(exception, chained);
        assertEquals("v2", exception.get("k2"));
        assertEquals(2, exception.getDetails().size());
    }

    @Test
    @DisplayName("CredentialIncorrectException should be instance of CredentialException")
    void shouldBeInstanceOfCredentialException() {
        assertInstanceOf(CredentialException.class, new CredentialIncorrectException("cred"));
    }
}
