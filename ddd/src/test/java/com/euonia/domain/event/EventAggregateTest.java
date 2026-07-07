package com.euonia.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link EventAggregate}。
 */
@SuppressWarnings("unused")
@DisplayName("EventAggregate")
class EventAggregateTest {

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should default id to null")
        void shouldDefaultIdToNull() {
            var aggregate = new EventAggregate();

            assertThat(aggregate.getId()).isNull();
        }

        @Test
        @DisplayName("should set and get id")
        void shouldSetAndGetId() {
            var aggregate = new EventAggregate();
            aggregate.setId("event-1");

            assertThat(aggregate.getId()).isEqualTo("event-1");
        }
    }

    @Nested
    @DisplayName("keys")
    class Keys {

        @Test
        @DisplayName("should return keys containing id")
        void shouldReturnKeysContainingId() {
            var aggregate = new EventAggregate();
            aggregate.setId("event-1");

            assertThat(aggregate.getKeys()).containsExactly("event-1");
        }
    }

    @Nested
    @DisplayName("event fields")
    class EventFields {

        @Test
        @DisplayName("should set and get event id")
        void shouldSetAndGetEventId() {
            var aggregate = new EventAggregate();
            aggregate.setEventId("evt-123");

            assertThat(aggregate.getEventId()).isEqualTo("evt-123");
        }

        @Test
        @DisplayName("should set and get type name")
        void shouldSetAndGetTypeName() {
            var aggregate = new EventAggregate();
            aggregate.setTypeName("com.example.OrderCreated");

            assertThat(aggregate.getTypeName()).isEqualTo("com.example.OrderCreated");
        }

        @Test
        @DisplayName("should set and get event intent")
        void shouldSetAndGetEventIntent() {
            var aggregate = new EventAggregate();
            aggregate.setEventIntent("OrderCreated");

            assertThat(aggregate.getEventIntent()).isEqualTo("OrderCreated");
        }

        @Test
        @DisplayName("should set and get timestamp")
        void shouldSetAndGetTimestamp() {
            var aggregate = new EventAggregate();
            aggregate.setTimestamp(1700000000000L);

            assertThat(aggregate.getTimestamp()).isEqualTo(1700000000000L);
        }

        @Test
        @DisplayName("should set and get originator type")
        void shouldSetAndGetOriginatorType() {
            var aggregate = new EventAggregate();
            aggregate.setOriginatorType("com.example.OrderAggregate");

            assertThat(aggregate.getOriginatorType()).isEqualTo("com.example.OrderAggregate");
        }

        @Test
        @DisplayName("should set and get originator id")
        void shouldSetAndGetOriginatorId() {
            var aggregate = new EventAggregate();
            aggregate.setOriginatorId("origin-456");

            assertThat(aggregate.getOriginatorId()).isEqualTo("origin-456");
        }

        @Test
        @DisplayName("should set and get event sequence")
        void shouldSetAndGetEventSequence() {
            var aggregate = new EventAggregate();
            aggregate.setEventSequence(42L);

            assertThat(aggregate.getEventSequence()).isEqualTo(42L);
        }

        @Test
        @DisplayName("should set and get event payload")
        void shouldSetAndGetEventPayload() {
            var aggregate = new EventAggregate();
            aggregate.setEventPayload("payload-data");

            assertThat(aggregate.getEventPayload()).isEqualTo("payload-data");
        }
    }

    @Nested
    @DisplayName("implements Aggregate")
    class ImplementsAggregate {

        @Test
        @DisplayName("should implement Aggregate<String>")
        void shouldImplementAggregate() {
            var aggregate = new EventAggregate();

            assertThat(aggregate).isInstanceOf(com.euonia.domain.Aggregate.class);
            assertThat(aggregate).isInstanceOf(com.euonia.domain.Entity.class);
        }
    }
}
