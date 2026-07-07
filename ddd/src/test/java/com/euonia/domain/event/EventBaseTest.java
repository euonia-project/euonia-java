package com.euonia.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link EventBase}。
 */
@SuppressWarnings("unused")
@DisplayName("EventBase")
class EventBaseTest {

    static class TestEvent extends EventBase {}

    @Nested
    @DisplayName("event intent")
    class EventIntent {

        @Test
        @DisplayName("should auto-set event intent to class name")
        void shouldAutoSetEventIntentToClassName() {
            var event = new TestEvent();

            assertThat(event.getEventIntent()).isEqualTo(TestEvent.class.getName());
        }

        @Test
        @DisplayName("should override event intent")
        void shouldOverrideEventIntent() {
            var event = new TestEvent();
            event.setEventIntent("CustomIntent");

            assertThat(event.getEventIntent()).isEqualTo("CustomIntent");
        }
    }

    @Nested
    @DisplayName("sequence")
    class Sequence {

        @Test
        @DisplayName("should default sequence to current time millis")
        void shouldDefaultSequenceToCurrentTimeMillis() {
            var before = System.currentTimeMillis();
            var event = new TestEvent();
            var after = System.currentTimeMillis();

            assertThat(event.getSequence()).isBetween(before, after);
        }

        @Test
        @DisplayName("should set and get sequence")
        void shouldSetAndGetSequence() {
            var event = new TestEvent();
            event.setSequence(42L);

            assertThat(event.getSequence()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("properties")
    class Properties {

        @Test
        @DisplayName("should set and get string property")
        void shouldSetAndGetStringProperty() {
            var event = new TestEvent();
            event.set("myKey", "myValue");

            assertThat(event.get("myKey")).isEqualTo("myValue");
        }

        @Test
        @DisplayName("should return null for missing property")
        void shouldReturnNullForMissingProperty() {
            var event = new TestEvent();

            assertThat(event.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("should get typed property via coercion")
        void shouldGetTypedPropertyViaCoercion() {
            var event = new TestEvent();
            event.set("count", "42");

            assertThat(event.get("count", Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("should return null for missing typed property")
        void shouldReturnNullForMissingTypedProperty() {
            var event = new TestEvent();

            assertThat(event.get("nonexistent", String.class)).isNull();
        }

        @Test
        @DisplayName("should coerce boolean values")
        void shouldCoerceBooleanValues() {
            var event = new TestEvent();
            event.set("enabled", "true");

            assertThat(event.get("enabled", Boolean.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("originator")
    class Originator {

        @Test
        @DisplayName("should set and get originator type")
        void shouldSetAndGetOriginatorType() {
            var event = new TestEvent();
            event.setOriginatorType("com.example.Order");

            assertThat(event.getOriginatorType()).isEqualTo("com.example.Order");
        }

        @Test
        @DisplayName("should set and get originator id")
        void shouldSetAndGetOriginatorId() {
            var event = new TestEvent();
            event.setOriginatorId("12345");

            assertThat(event.getOriginatorId()).isEqualTo("12345");
        }
    }
}
