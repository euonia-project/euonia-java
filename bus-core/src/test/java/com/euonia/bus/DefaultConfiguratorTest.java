package com.euonia.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link DefaultConfigurator} 配置门面的流式 API。
 */
@DisplayName("DefaultConfigurator")
class DefaultConfiguratorTest {

    @Nested
    @DisplayName("convention configuration")
    class ConventionConfig {

        @Test
        @DisplayName("should configure convention via callback")
        void shouldConfigureConvention() {
            var configurator = new DefaultConfigurator();
            configurator.setConvention(c ->
                c.evaluateUnicast(t -> t == String.class));

            var convention = configurator.getConventionBuilder().getConvention();

            assertThat(convention.isUnicastType(String.class)).isTrue();
            assertThat(convention.isUnicastType(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("strategy configuration")
    class StrategyConfig {

        @Test
        @DisplayName("should add strategy builder by name")
        void shouldAddStrategy() {
            var configurator = new DefaultConfigurator();
            configurator.setStrategy("rabbitmq", s ->
                s.evaluateOutgoing(t -> t == Integer.class));

            assertThat(configurator.getStrategyBuilders()).containsKey("rabbitmq");
            var strategy = configurator.getStrategy("rabbitmq");
            assertThat(strategy.outgoing(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("should reject empty strategy name")
        void shouldRejectEmptyName() {
            var configurator = new DefaultConfigurator();

            assertThatThrownBy(() -> configurator.setStrategy("", s -> {}))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject null strategy config")
        void shouldRejectNullConfig() {
            var configurator = new DefaultConfigurator();

            assertThatThrownBy(() -> configurator.setStrategy("test", null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("handler registration")
    class HandlerRegistrationTests {

        @Test
        @DisplayName("should accept single registration")
        void shouldAcceptSingleRegistration() {
            var configurator = new DefaultConfigurator();
            var reg = new HandlerRegistration(
                "ch", String.class, Object.class, null);

            configurator.registerHandlers(reg);

            assertThat(configurator.getRegistrations()).contains(reg);
        }

        @Test
        @DisplayName("should reject null registration")
        void shouldRejectNullRegistration() {
            var configurator = new DefaultConfigurator();
            assertThatThrownBy(() ->
                configurator.registerHandlers((HandlerRegistration) null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
