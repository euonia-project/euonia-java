package com.euonia.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountExpiredException")
class AccountExpiredExceptionTest {

    @Test
    @DisplayName("Given identity and optional message cause when constructing then properties are preserved")
    void givenConstructorsWhenCreatingThenPropertiesArePreserved() {
        Throwable cause = new IllegalStateException("expired");

        AccountExpiredException oneArg = new AccountExpiredException("user1");
        AccountExpiredException twoArg = new AccountExpiredException("user2", "msg");
        AccountExpiredException threeArg = new AccountExpiredException("user3", "msg2", cause);

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
        AccountExpiredException exception = new AccountExpiredException("user");

        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails().isEmpty());
        assertNull(exception.get("missing"));
        assertThrows(UnsupportedOperationException.class, () -> exception.set("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> exception.with("k", "v"));
    }

    @Test
    @DisplayName("AccountExpiredException should be instance of AccountException")
    void shouldBeInstanceOfAccountException() {
        assertInstanceOf(AccountException.class, new AccountExpiredException("user"));
    }
}
