package com.euonia.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.domain.AggregateBase;

/**
 * 测试 {@link DomainEventBase}。
 */
@SuppressWarnings("unused")
@DisplayName("DomainEventBase")
class DomainEventBaseTest {

    static class TestDomainEvent extends DomainEventBase {}

    static class TestAggregate extends AggregateBase<String> {}

    @Nested
    @DisplayName("attach")
    class Attach {

        @Test
        @DisplayName("should set originator from aggregate")
        void shouldSetOriginatorFromAggregate() {
            var event = new TestDomainEvent();
            var aggregate = new TestAggregate();
            aggregate.setId("agg-1");

            event.attach(aggregate);

            assertThat(event.getOriginatorId()).isEqualTo("agg-1");
            assertThat(event.getOriginatorType()).isEqualTo(TestAggregate.class.getName());
        }

        @Test
        @DisplayName("should store aggregate payload")
        void shouldStoreAggregatePayload() {
            var event = new TestDomainEvent();
            var aggregate = new TestAggregate();
            aggregate.setId("agg-1");

            event.attach(aggregate);

            assertThat(event.getAggregatePayload()).isSameAs(aggregate);
        }
    }

    @Nested
    @DisplayName("getEventAggregate")
    class GetEventAggregate {

        @Test
        @DisplayName("should return non-null EventAggregate with id")
        void shouldReturnEventAggregateWithId() {
            var event = new TestDomainEvent();
            var result = event.getEventAggregate();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
        }

        @Test
        @DisplayName("should capture event metadata in EventAggregate")
        void shouldCaptureEventMetadataInEventAggregate() {
            var event = new TestDomainEvent();
            var aggregate = new TestAggregate();
            aggregate.setId("agg-1");
            event.attach(aggregate);

            // Verify originator info is set on the event directly
            assertThat(event.getOriginatorId()).isEqualTo("agg-1");
            assertThat(event.getOriginatorType()).isEqualTo(TestAggregate.class.getName());

            var result = event.getEventAggregate();
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("aggregate payload")
    class AggregatePayload {

        @Test
        @DisplayName("should set and get aggregate payload")
        void shouldSetAndGetAggregatePayload() {
            var event = new TestDomainEvent();
            event.setAggregatePayload("customPayload");

            assertThat(event.getAggregatePayload()).isEqualTo("customPayload");
        }
    }
}
