package com.euonia.reflection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link LazyServiceProvider}。
 */
@SuppressWarnings("unused")
@DisplayName("LazyServiceProvider")
class LazyServiceProviderTest {

    static class CountingProvider implements ServiceProvider {
        final AtomicInteger serviceCalls = new AtomicInteger();
        final AtomicInteger listCalls = new AtomicInteger();
        private final Object value;

        CountingProvider(Object value) {
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> getService(Class<T> type) {
            serviceCalls.incrementAndGet();
            return Optional.ofNullable((T) value);
        }

        @Override
        public <T> Optional<T> getService(Class<T> type, Class<?>... genericTypeArguments) {
            return getService(type);
        }

        @Override
        public <T> Optional<T> getService(Class<T> type, String serviceName) {
            return getService(type);
        }

        @Override
        public <T> T getRequiredService(Class<T> type) {
            return getService(type).orElseThrow();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> getServices(Class<T> type) {
            listCalls.incrementAndGet();
            return value == null ? List.of() : List.of((T) value);
        }

        @Override
        public <T> List<T> getServices(Class<T> type, Class<?>... genericTypeArguments) {
            return getServices(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T createInstance(Class<T> type, Object... constructorArguments) {
            return (T) value;
        }
    }

    @Nested
    @DisplayName("service caching")
    class ServiceCaching {

        @Test
        @DisplayName("should resolve service only once")
        void shouldResolveServiceOnlyOnce() {
            var provider = new CountingProvider("service");
            var lazy = new LazyServiceProvider(provider);

            var first = lazy.getService(String.class);
            var second = lazy.getService(String.class);

            assertThat(first).contains("service");
            assertThat(second).contains("service");
            assertThat(provider.serviceCalls).hasValue(1);
        }

        @Test
        @DisplayName("should cache empty results")
        void shouldCacheEmptyResults() {
            var provider = new CountingProvider(null);
            var lazy = new LazyServiceProvider(provider);

            assertThat(lazy.getService(String.class)).isEmpty();
            assertThat(lazy.getService(String.class)).isEmpty();
            assertThat(provider.serviceCalls).hasValue(1);
        }

        @Test
        @DisplayName("should resolve required service and throw when absent")
        void shouldResolveRequiredService() {
            var lazy = new LazyServiceProvider(new CountingProvider("service"));

            assertThat(lazy.getRequiredService(String.class)).isEqualTo("service");
            assertThatIllegalStateException().isThrownBy(() ->
                new LazyServiceProvider(new CountingProvider(null)).getRequiredService(String.class));
        }

        @Test
        @DisplayName("should cache service lists")
        void shouldCacheServiceLists() {
            var provider = new CountingProvider("service");
            var lazy = new LazyServiceProvider(provider);

            assertThat(lazy.getServices(String.class)).containsExactly("service");
            assertThat(lazy.getServices(String.class)).containsExactly("service");
            assertThat(provider.listCalls).hasValue(1);
        }

        @Test
        @DisplayName("should delegate createInstance without caching")
        void shouldDelegateCreateInstance() {
            var provider = new CountingProvider("created");
            var lazy = new LazyServiceProvider(provider);

            assertThat(lazy.createInstance(String.class)).isEqualTo("created");
            assertThat(provider.serviceCalls).hasValue(0);
        }
    }

    @Nested
    @DisplayName("factory and validation")
    class FactoryAndValidation {

        @Test
        @DisplayName("should return same instance when already lazy")
        void shouldReturnSameInstanceWhenAlreadyLazy() {
            var lazy = new LazyServiceProvider(new CountingProvider(null));

            assertThat(LazyServiceProvider.of(lazy)).isSameAs(lazy);
        }

        @Test
        @DisplayName("should reject null provider")
        void shouldRejectNullProvider() {
            assertThatNullPointerException().isThrownBy(() -> new LazyServiceProvider(null));
        }
    }
}
