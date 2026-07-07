package com.euonia.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link ApplicationEventBase}。
 */
@SuppressWarnings("unused")
@DisplayName("ApplicationEventBase")
class ApplicationEventBaseTest {

    static class TestAppEvent extends ApplicationEventBase {}

    @Test
    @DisplayName("should implement ApplicationEvent")
    void shouldImplementApplicationEvent() {
        var event = new TestAppEvent();

        assertThat(event).isInstanceOf(ApplicationEvent.class);
        assertThat(event).isInstanceOf(Event.class);
    }

    @Test
    @DisplayName("should extend EventBase")
    void shouldExtendEventBase() {
        var event = new TestAppEvent();

        assertThat(event).isInstanceOf(EventBase.class);
    }

    @Test
    @DisplayName("should auto-set event intent from class name")
    void shouldAutoSetEventIntentFromClassName() {
        var event = new TestAppEvent();

        assertThat(event.getEventIntent()).isEqualTo(TestAppEvent.class.getName());
    }
}
