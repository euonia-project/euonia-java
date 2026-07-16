package com.euonia.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestContextAwareExecutor")
class RequestContextAwareExecutorTest {

    @Test
    @DisplayName("Given fromCommonPool when calling then returns non-null executor")
    void givenFromCommonPoolWhenCallingThenReturnsNonNullExecutor() {
        Executor executor = RequestContextAwareExecutor.fromCommonPool();

        assertNotNull(executor);
    }

    @Test
    @DisplayName("Given fromExecutor when calling then returns non-null executor")
    void givenFromExecutorWhenCallingThenReturnsNonNullExecutor() {
        Executor executor = RequestContextAwareExecutor.fromExecutor();

        assertNotNull(executor);
    }

    @Test
    @DisplayName("Given fromExecutor when executing runnable then runnable completes")
    void givenFromExecutorWhenExecutingRunnableThenRunnableCompletes() throws InterruptedException {
        Executor executor = RequestContextAwareExecutor.fromExecutor();
        boolean[] executed = {false};

        executor.execute(() -> executed[0] = true);
        Thread.sleep(100);

        assertTrue(executed[0]);
    }
}
