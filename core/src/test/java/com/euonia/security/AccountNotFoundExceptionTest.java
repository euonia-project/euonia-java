package com.euonia.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountNotFoundException")
class AccountNotFoundExceptionTest {

    @Test
    @DisplayName("Given identity and optional message cause when constructing then properties are preserved")
    void givenConstructorsWhenCreatingThenPropertiesArePreserved() {
        Throwable cause = new IllegalStateException("not found");

        AccountNotFoundException oneArg = new AccountNotFoundException("user1");
        AccountNotFoundException twoArg = new AccountNotFoundException("user2", "msg");
        AccountNotFoundException threeArg = new AccountNotFoundException("user3", "msg2", cause);

        assertEquals("user1", oneArg.getIdentity());
        assertNull(oneArg.getMessage());
        assertEquals("user2", twoArg.getIdentity());
        assertEquals("msg", twoArg.getMessage());
        assertEquals("user3", threeArg.getIdentity());
        assertEquals("msg2", threeArg.getMessage());
        assertSame(cause, threeArg.getCause());
    }

    @Test
    @DisplayName("Given details map when setting values then unsupported operation is thrown by immutable backing map")
    void givenDetailsWhenSettingThenThrowUnsupportedOperation() {
        AccountNotFoundException exception = new AccountNotFoundException("user");

        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails().isEmpty());
        assertNull(exception.get("missing"));
        assertThrows(UnsupportedOperationException.class, () -> exception.set("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> exception.with("k", "v"));
    }

    @Test
    @DisplayName("AccountNotFoundException should be instance of AccountException")
    void shouldBeInstanceOfAccountException() {
        assertInstanceOf(AccountException.class, new AccountNotFoundException("user"));
    }
}
