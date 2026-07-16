package com.euonia.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestContextCopyingDecorator")
class RequestContextCopyingDecoratorTest {

    @Test
    @DisplayName("Given decorator when decorating runnable then returned runnable is not null")
    void givenDecoratorWhenDecoratingRunnableThenReturnedRunnableIsNotNull() {
        RequestContextCopyingDecorator decorator = new RequestContextCopyingDecorator();
        Runnable decorated = decorator.decorate(() -> {});

        assertNotNull(decorated);
    }

    @Test
    @DisplayName("Given decorator when decorating then runnable executes successfully")
    void givenDecoratorWhenDecoratingThenRunnableExecutesSuccessfully() {
        RequestContextCopyingDecorator decorator = new RequestContextCopyingDecorator();
        boolean[] executed = {false};
        Runnable decorated = decorator.decorate(() -> executed[0] = true);

        decorated.run();

        assertTrue(executed[0]);
    }
}
