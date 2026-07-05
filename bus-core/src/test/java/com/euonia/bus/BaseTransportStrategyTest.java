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

            assertThat(strategy.allowOutgoing("java.lang.String", String.class)).isFalse();
            assertThat(strategy.allowIncoming("java.lang.String", String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("custom definitions")
    class CustomDefinitions {

        @Test
        @DisplayName("should use defineOutgoingStrategy")
        void shouldUseDefineOutgoing() {
            var strategy = new BaseTransportStrategy();
            strategy.defineOutgoingStrategy((ch, mt) -> mt == Integer.class);

            assertThat(strategy.allowOutgoing("java.lang.Integer", Integer.class)).isTrue();
            assertThat(strategy.allowOutgoing("java.lang.String", String.class)).isFalse();
        }

        @Test
        @DisplayName("should use defineIncomingStrategy")
        void shouldUseDefineIncoming() {
            var strategy = new BaseTransportStrategy();
            strategy.defineIncomingStrategy((ch, mt) -> mt == Integer.class);

            assertThat(strategy.allowIncoming("java.lang.Integer", Integer.class)).isTrue();
            assertThat(strategy.allowIncoming("java.lang.String", String.class)).isFalse();
        }

        @Test
        @DisplayName("should evaluate added strategies")
        void shouldEvaluateAddedStrategies() {
            var strategy = new BaseTransportStrategy();
            strategy.add(new TransportStrategy() {
                @Override
                public String getName() { return "test"; }

                @Override
                public boolean allowOutgoing(String ch, Class<?> mt) { return mt == Long.class; }

                @Override
                public boolean allowIncoming(String ch, Class<?> mt) { return false; }
            });

            assertThat(strategy.allowOutgoing("java.lang.Long", Long.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("caching")
    class Caching {

        @Test
        @DisplayName("should cache repeated lookups")
        void shouldCacheLookups() {
            var strategy = new BaseTransportStrategy();
            strategy.defineOutgoingStrategy((ch, mt) -> true);

            // Two calls should both return true from cache
            assertThat(strategy.allowOutgoing("java.lang.String", String.class)).isTrue();
            assertThat(strategy.allowOutgoing("java.lang.String", String.class)).isTrue();
        }
    }
}
