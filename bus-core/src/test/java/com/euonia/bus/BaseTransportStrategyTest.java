package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.strategy.BaseTransportStrategy;
import com.euonia.bus.strategy.TransportStrategy;

/**
 * 测试 {@link BaseTransportStrategy} 的组合和缓存逻辑。
 */
@SuppressWarnings("unused")
@DisplayName("BaseTransportStrategy")
class BaseTransportStrategyTest {

    @Nested
    @DisplayName("default strategy")
    class DefaultStrategy {

        @Test
        @DisplayName("should return false for any channel by default")
        void shouldReturnFalseByDefault() {
            var strategy = new BaseTransportStrategy();

            assertThat(strategy.allowOutgoing("java.lang.String")).isFalse();
            assertThat(strategy.allowIncoming("java.lang.String")).isFalse();
        }
    }

    @Nested
    @DisplayName("custom definitions")
    class CustomDefinitions {

        @Test
        @DisplayName("should use defineOutgoingStrategy")
        void shouldUseDefineOutgoing() {
            var strategy = new BaseTransportStrategy();
            strategy.defineOutgoingStrategy(t -> t.equals("java.lang.Integer"));

            assertThat(strategy.allowOutgoing("java.lang.Integer")).isTrue();
            assertThat(strategy.allowOutgoing("java.lang.String")).isFalse();
        }

        @Test
        @DisplayName("should use defineIncomingStrategy")
        void shouldUseDefineIncoming() {
            var strategy = new BaseTransportStrategy();
            strategy.defineIncomingStrategy(t -> t.equals("java.lang.Integer"));

            assertThat(strategy.allowIncoming("java.lang.Integer")).isTrue();
            assertThat(strategy.allowIncoming("java.lang.String")).isFalse();
        }

        @Test
        @DisplayName("should evaluate added strategies")
        void shouldEvaluateAddedStrategies() {
            var strategy = new BaseTransportStrategy();
            strategy.add(new TransportStrategy() {
                @Override
                public String getName() { return "test"; }

                @Override
                public boolean allowOutgoing(String ch) { return ch.equals("java.lang.Long"); }

                @Override
                public boolean allowIncoming(String ch) { return false; }
            });

            assertThat(strategy.allowOutgoing("java.lang.Long")).isTrue();
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
            assertThat(strategy.allowOutgoing("java.lang.String")).isTrue();
            assertThat(strategy.allowOutgoing("java.lang.String")).isTrue();
        }
    }
}
