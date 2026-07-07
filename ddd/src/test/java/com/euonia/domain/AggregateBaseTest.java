package com.euonia.domain;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.domain.event.DomainEvent;
import com.euonia.domain.event.EventAggregate;

/**
 * 测试 {@link AggregateBase} 的领域事件管理。
 */
@SuppressWarnings("unused")
@DisplayName("AggregateBase")
class AggregateBaseTest {

    static class OrderAggregate extends AggregateBase<String> {}

    static class OrderCreatedEvent implements DomainEvent {
        String orderId;
        Aggregate<?> attachedAggregate;

        OrderCreatedEvent(String orderId) { this.orderId = orderId; }

        @Override public <ID extends Comparable<ID>> void attach(Aggregate<ID> aggregate) {
            this.attachedAggregate = aggregate;
        }
        @Override public EventAggregate getEventAggregate() { return null; }
        @Override public long getSequence() { return 0; }
        @Override public void setSequence(long sequence) {}
        @Override public String getEventIntent() { return "OrderCreated"; }
        @Override public void setEventIntent(String intent) {}
        @Override public String getOriginatorType() { return OrderAggregate.class.getName(); }
        @Override public void setOriginatorType(String type) {}
        @Override public String getOriginatorId() { return orderId; }
        @Override public void setOriginatorId(String id) {}
    }

    @Nested
    @DisplayName("event raising")
    class EventRaising {

        @Test
        @DisplayName("should add event to events list")
        void shouldAddEventToEventsList() {
            var aggregate = new OrderAggregate();
            var event = new OrderCreatedEvent("order-1");

            aggregate.raiseEvent(event);

            assertThat(aggregate.getEvents()).containsExactly(event);
        }

        @Test
        @DisplayName("should call registered handler when event is raised")
        void shouldCallRegisteredHandlerWhenEventRaised() {
            var aggregate = new OrderAggregate();
            var counter = new AtomicInteger(0);

            aggregate.registerEvent(OrderCreatedEvent.class, e -> counter.incrementAndGet());
            aggregate.raiseEvent(new OrderCreatedEvent("order-1"));

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should accumulate multiple events")
        void shouldAccumulateMultipleEvents() {
            var aggregate = new OrderAggregate();
            aggregate.raiseEvent(new OrderCreatedEvent("order-1"));
            aggregate.raiseEvent(new OrderCreatedEvent("order-2"));

            assertThat(aggregate.getEvents()).hasSize(2);
        }

        @Test
        @DisplayName("should return unmodifiable events view")
        void shouldReturnUnmodifiableEventsView() {
            var aggregate = new OrderAggregate();

            List<DomainEvent> events = aggregate.getEvents();

            assertThat(events).isEmpty();
            assertThatThrownBy(() -> events.add(new OrderCreatedEvent("test")))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("event clearing")
    class EventClearing {

        @Test
        @DisplayName("should clear all events")
        void shouldClearAllEvents() {
            var aggregate = new OrderAggregate();
            aggregate.raiseEvent(new OrderCreatedEvent("order-1"));
            aggregate.clearEvents();

            assertThat(aggregate.getEvents()).isEmpty();
        }

        @Test
        @DisplayName("should be safe to clear when already empty")
        void shouldBeSafeToClearWhenAlreadyEmpty() {
            var aggregate = new OrderAggregate();

            aggregate.clearEvents();

            assertThat(aggregate.getEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("attachEvents")
    class AttachEvents {

        @Test
        @DisplayName("should attach all raised events to the aggregate")
        void shouldAttachAllRaisedEvents() {
            var aggregate = new OrderAggregate();
            var event = new OrderCreatedEvent("order-1");
            aggregate.raiseEvent(event);

            aggregate.attachEvents();

            assertThat(event.attachedAggregate).isSameAs(aggregate);
        }
    }
}
