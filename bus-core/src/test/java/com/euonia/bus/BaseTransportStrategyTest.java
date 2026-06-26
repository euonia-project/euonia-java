package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;

import com.euonia.bus.MessageConventionType;
import com.euonia.bus.strategy.BaseTransportStrategy;
import com.euonia.bus.strategy.TransportStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link BaseTransportStrategy} 的组合和缓存逻辑。
 */
@DisplayName("BaseTransportStrategy")
class BaseTransportStrategyTest {

    @Nested
    @DisplayName("default strategy")
    class DefaultStrategy {

        @Test
        @DisplayName("should return false for any type by default")
        void shouldReturnFalseByDefault() {
            var strategy = new BaseTransportStrategy();

            assertThat(strategy.outgoing(String.class)).isFalse();
            assertThat(strategy.incoming(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("custom definitions")
    class CustomDefinitions {

        @Test
        @DisplayName("should use defineOutgoingStrategy")
        void shouldUseDefineOutgoing() {
            var strategy = new BaseTransportStrategy();
            strategy.defineOutgoingStrategy(t -> t == Integer.class);

            assertThat(strategy.outgoing(Integer.class)).isTrue();
            assertThat(strategy.outgoing(String.class)).isFalse();
        }

        @Test
        @DisplayName("should use defineIncomingStrategy")
        void shouldUseDefineIncoming() {
            var strategy = new BaseTransportStrategy();
            strategy.defineIncomingStrategy(t -> t == Integer.class);

            assertThat(strategy.incoming(Integer.class)).isTrue();
            assertThat(strategy.incoming(String.class)).isFalse();
        }

        @Test
        @DisplayName("should evaluate added strategies")
        void shouldEvaluateAddedStrategies() {
            var strategy = new BaseTransportStrategy();
            strategy.add(new TransportStrategy() {
                @Override
                public String getName() { return "test"; }

                @Override
                public boolean outgoing(Class<?> t) { return t == Long.class; }

                @Override
                public boolean incoming(Class<?> t) { return false; }
            });

            assertThat(strategy.outgoing(Long.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("caching")
    class Caching {

        @Test
        @DisplayName("should cache repeated lookups")
        void shouldCacheLookups() {
            var strategy = new BaseTransportStrategy();
            strategy.defineOutgoingStrategy(t -> true);

            // Two calls should both return true from cache
            assertThat(strategy.outgoing(String.class)).isTrue();
            assertThat(strategy.outgoing(String.class)).isTrue();
        }
    }
}
