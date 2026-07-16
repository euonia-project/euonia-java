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
    @DisplayName("Given details map when setting values then values are stored and retrievable")
    void givenDetailsWhenSettingThenValuesAreStored() {
        AccountNotFoundException exception = new AccountNotFoundException("user");

        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails().isEmpty());
        assertNull(exception.get("missing"));

        exception.set("k", "v");
        assertEquals("v", exception.get("k"));
        assertEquals(1, exception.getDetails().size());

        AccountException chained = exception.with("k2", "v2");
        assertSame(exception, chained);
        assertEquals("v2", exception.get("k2"));
        assertEquals(2, exception.getDetails().size());
    }

    @Test
    @DisplayName("AccountNotFoundException should be instance of AccountException")
    void shouldBeInstanceOfAccountException() {
        assertInstanceOf(AccountException.class, new AccountNotFoundException("user"));
    }
}
