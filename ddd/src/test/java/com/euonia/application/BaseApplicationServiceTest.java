package com.euonia.application;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.bus.Bus;
import com.euonia.http.RequestContext;
import com.euonia.http.RequestContextAccessor;
import com.euonia.reflection.LazyServiceProvider;
import com.euonia.reflection.ServiceProvider;
import com.euonia.security.UserPrincipal;
import com.euonia.usecase.UseCaseExecutor;

/**
 * 测试 {@link BaseApplicationService}。
 */
@SuppressWarnings("unused")
@DisplayName("BaseApplicationService")
class BaseApplicationServiceTest {

    static class TestAppService extends BaseApplicationService {
        TestAppService(ServiceProvider provider) {
            super(provider);
        }

        TestAppService() {
            super();
        }

        // Expose protected methods for testing
        <T> Optional<T> publicGetService(Class<T> type) {
            return getService(type);
        }

        Bus publicGetBus() {
            return getBus();
        }

        UserPrincipal publicGetUser() {
            return getUser();
        }

        RequestContext publicGetRequestContext() {
            return getRequestContext();
        }

        org.slf4j.Logger publicGetLogger() {
            return getLogger();
        }

        UseCaseExecutor publicGetExecutor() {
            return getExecutor();
        }
    }

    private static <T> T proxy(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> null));
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextAccessor.remove();
    }

    @Nested
    @DisplayName("service resolution")
    class ServiceResolution {

        @Test
        @DisplayName("should resolve registered service")
        void shouldResolveRegisteredService() {
            var myService = "hello";
            var provider = StubServiceProvider.with(String.class, myService);
            var svc = new TestAppService(provider);

            var result = svc.publicGetService(String.class);

            assertThat(result).isPresent().containsSame(myService);
        }

        @Test
        @DisplayName("should return empty for unregistered service")
        void shouldReturnEmptyForUnregisteredService() {
            var provider = new StubServiceProvider();
            var svc = new TestAppService(provider);

            var result = svc.publicGetService(Integer.class);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should wrap provider with lazy service provider")
        void shouldWrapProviderWithLazyServiceProvider() {
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.getLazyServiceProvider()).isInstanceOf(LazyServiceProvider.class);
        }

        @Test
        @DisplayName("should throw when provider is not initialized")
        void shouldThrowWhenProviderIsNotInitialized() {
            var svc = new TestAppService();

            assertThatIllegalStateException().isThrownBy(() -> svc.publicGetService(String.class));
        }
    }

    @Nested
    @DisplayName("bus")
    class BusResolution {

        @Test
        @DisplayName("should resolve bus from provider")
        void shouldResolveBusFromProvider() {
            var bus = proxy(Bus.class);
            var provider = StubServiceProvider.with(Bus.class, bus);
            var svc = new TestAppService(provider);

            assertThat(svc.publicGetBus()).isSameAs(bus);
        }

        @Test
        @DisplayName("should return null when bus not registered")
        void shouldReturnNullWhenBusNotRegistered() {
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetBus()).isNull();
        }
    }

    @Nested
    @DisplayName("user principal")
    class UserPrincipalTests {

        @Test
        @DisplayName("should resolve user from provider first")
        void shouldResolveUserFromProvider() {
            var providerUser = new UserPrincipal(new javax.security.auth.Subject());
            var contextUser = new UserPrincipal(new javax.security.auth.Subject());
            var context = new RequestContext();
            context.setUser(contextUser);
            RequestContextAccessor.set(context);

            var svc = new TestAppService(StubServiceProvider.with(UserPrincipal.class, providerUser));

            assertThat(svc.publicGetUser()).isSameAs(providerUser);
        }

        @Test
        @DisplayName("should fall back to current request context")
        void shouldFallBackToRequestContext() {
            var user = new UserPrincipal(new javax.security.auth.Subject());
            var context = new RequestContext();
            context.setUser(user);
            RequestContextAccessor.set(context);

            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetUser()).isSameAs(user);
        }

        @Test
        @DisplayName("should return null when user is unavailable")
        void shouldReturnNullWhenUserIsUnavailable() {
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetUser()).isNull();
        }
    }

    @Nested
    @DisplayName("request context")
    class RequestContextTests {

        @Test
        @DisplayName("should return current request context")
        void shouldReturnCurrentRequestContext() {
            var context = new RequestContext();
            RequestContextAccessor.set(context);
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetRequestContext()).isSameAs(context);
        }

        @Test
        @DisplayName("should return null outside request flow")
        void shouldReturnNullOutsideRequestFlow() {
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetRequestContext()).isNull();
        }
    }

    @Nested
    @DisplayName("logger and executor")
    class LoggerAndExecutorTests {

        @Test
        @DisplayName("should create logger for the concrete service type")
        void shouldCreateLoggerForConcreteServiceType() {
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetLogger()).isNotNull();
            assertThat(svc.publicGetLogger().getName()).isEqualTo(TestAppService.class.getName());
        }

        @Test
        @DisplayName("should resolve executor from provider")
        void shouldResolveExecutorFromProvider() {
            var executor = proxy(UseCaseExecutor.class);
            var svc = new TestAppService(StubServiceProvider.with(UseCaseExecutor.class, executor));

            assertThat(svc.publicGetExecutor()).isSameAs(executor);
        }

        @Test
        @DisplayName("should return null when executor not registered")
        void shouldReturnNullWhenExecutorNotRegistered() {
            var svc = new TestAppService(new StubServiceProvider());

            assertThat(svc.publicGetExecutor()).isNull();
        }
    }
}
