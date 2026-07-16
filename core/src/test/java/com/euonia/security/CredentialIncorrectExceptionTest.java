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
    @DisplayName("Given details map when setting values then unsupported operation is thrown by immutable backing map")
    void givenDetailsWhenSettingThenThrowUnsupportedOperation() {
        CredentialIncorrectException exception = new CredentialIncorrectException("credential");

        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails().isEmpty());
        assertNull(exception.get("missing"));
        assertThrows(UnsupportedOperationException.class, () -> exception.set("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> exception.with("k", "v"));
    }

    @Test
    @DisplayName("CredentialIncorrectException should be instance of CredentialException")
    void shouldBeInstanceOfCredentialException() {
        assertInstanceOf(CredentialException.class, new CredentialIncorrectException("cred"));
    }
}
