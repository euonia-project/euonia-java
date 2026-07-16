package com.euonia.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CredentialNotFoundException")
class CredentialNotFoundExceptionTest {

    @Test
    @DisplayName("Given credential and optional message cause when constructing then properties are preserved")
    void givenConstructorsWhenCreatingThenPropertiesArePreserved() {
        Throwable cause = new IllegalArgumentException("not found");

        CredentialNotFoundException oneArg = new CredentialNotFoundException("cred1");
        CredentialNotFoundException twoArg = new CredentialNotFoundException("cred2", "msg");
        CredentialNotFoundException threeArg = new CredentialNotFoundException("cred3", "msg2", cause);

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
        CredentialNotFoundException exception = new CredentialNotFoundException("credential");

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
    @DisplayName("CredentialNotFoundException should be instance of CredentialException")
    void shouldBeInstanceOfCredentialException() {
        assertInstanceOf(CredentialException.class, new CredentialNotFoundException("cred"));
    }
}
