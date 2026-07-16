package com.euonia.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestContextAccessor")
class RequestContextAccessorTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        RequestContextAccessor.remove();
    }

    @Test
    @DisplayName("Given no context set when getting then returns null")
    void givenNoContextSetWhenGettingThenReturnsNull() {
        assertNull(RequestContextAccessor.get());
    }

    @Test
    @DisplayName("Given context set when getting then returns same context")
    void givenContextSetWhenGettingThenReturnsSameContext() {
        RequestContext context = new RequestContext();
        context.setConnectionId("conn-1");

        RequestContextAccessor.set(context);

        assertSame(context, RequestContextAccessor.get());
        assertEquals("conn-1", RequestContextAccessor.get().getConnectionId());
    }

    @Test
    @DisplayName("Given context set when removing then subsequent get returns null")
    void givenContextSetWhenRemovingThenSubsequentGetReturnsNull() {
        RequestContext context = new RequestContext();
        RequestContextAccessor.set(context);
        assertEquals(context, RequestContextAccessor.get());

        RequestContextAccessor.remove();

        assertNull(RequestContextAccessor.get());
    }

    @Test
    @DisplayName("Given null context when setting then get returns null")
    void givenNullContextWhenSettingThenGetReturnsNull() {
        RequestContextAccessor.set(null);

        assertNull(RequestContextAccessor.get());
    }

    @Test
    @DisplayName("Given overwriting context when setting again then returns latest")
    void givenOverwritingContextWhenSettingAgainThenReturnsLatest() {
        RequestContext first = new RequestContext();
        first.setConnectionId("first");
        RequestContext second = new RequestContext();
        second.setConnectionId("second");

        RequestContextAccessor.set(first);
        RequestContextAccessor.set(second);

        assertEquals("second", RequestContextAccessor.get().getConnectionId());
    }
}
