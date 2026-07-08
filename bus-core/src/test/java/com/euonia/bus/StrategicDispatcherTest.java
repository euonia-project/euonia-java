package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.exception.MessageTypeException;

/**
 * 测试 {@link StrategicDispatcher}。
 */
@SuppressWarnings("unused")
@DisplayName("StrategicDispatcher")
class StrategicDispatcherTest {

    private static DefaultConfigurator createConfigurator(String defaultTransport) {
        var configurator = new DefaultConfigurator();
        if (defaultTransport != null) {
            configurator.setDefaultTransport(() -> defaultTransport);
        }
        return configurator;
    }

    @Nested
    @DisplayName("default transport fallback")
    class DefaultTransportFallback {

        @Test
        @DisplayName("should use default transport when no strategy matches")
        void shouldUseDefaultTransportWhenNoStrategyMatches() {
            var configurator = createConfigurator("inmemory");
            var dispatcher = new StrategicDispatcher(configurator);

            var transports = dispatcher.determine("any-channel", String.class);

            assertThat(transports).containsExactly("inmemory");
        }
    }

    @Nested
    @DisplayName("strategy routing")
    class StrategyRouting {

        @Test
        @DisplayName("should route to matching strategy")
        void shouldRouteToMatchingStrategy() {
            var configurator = createConfigurator(null);
            configurator.setStrategy("kafka", s -> s.evaluateOutgoing((ch, mt) -> mt == Integer.class));

            var dispatcher = new StrategicDispatcher(configurator);

            var transports = dispatcher.determine("orders", Integer.class);

            assertThat(transports).containsExactly("kafka");
        }

        @Test
        @DisplayName("should route to multiple strategies for multicast")
        void shouldRouteToMultipleStrategiesForMulticast() {
            var configurator = createConfigurator(null);
            configurator.setConvention(c -> c.evaluateMulticast(ch -> "events".equals(ch)));
            configurator.setStrategy("kafka", s -> s.evaluateOutgoing((ch, mt) -> mt == String.class));
            configurator.setStrategy("rabbitmq", s -> s.evaluateOutgoing((ch, mt) -> mt == String.class));

            var dispatcher = new StrategicDispatcher(configurator);

            var transports = dispatcher.determine("events", String.class);

            assertThat(transports).containsExactlyInAnyOrder("kafka", "rabbitmq");
        }

        @Test
        @DisplayName("should throw when multiple strategies match non-multicast channel")
        void shouldThrowWhenMultipleStrategiesMatchNonMulticast() {
            var configurator = createConfigurator(null);
            configurator.setConvention(c -> c.evaluateUnicast(ch -> "commands".equals(ch)));
            configurator.setStrategy("kafka", s -> s.evaluateOutgoing((ch, mt) -> mt == Integer.class));
            configurator.setStrategy("rabbitmq", s -> s.evaluateOutgoing((ch, mt) -> mt == Integer.class));

            var dispatcher = new StrategicDispatcher(configurator);

            assertThatThrownBy(() -> dispatcher.determine("commands", Integer.class))
                .isInstanceOf(MessageTypeException.class)
                .hasMessageContaining("not identified as a multicast type");
        }
    }

    @Nested
    @DisplayName("error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw when no transport configured")
        void shouldThrowWhenNoTransportConfigured() {
            var configurator = createConfigurator(null);
            var dispatcher = new StrategicDispatcher(configurator);

            assertThatThrownBy(() -> dispatcher.determine("orders", String.class))
                .isInstanceOf(MessageTypeException.class)
                .hasMessageContaining("No transport is configured");
        }
    }
}
