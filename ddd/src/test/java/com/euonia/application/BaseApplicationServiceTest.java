package com.euonia.application;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.http.RequestContext;
import com.euonia.http.RequestContextAccessor;
import com.euonia.reflection.ServiceProvider;
import com.euonia.security.UserPrincipal;

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

        // Expose protected method for testing
        <T> Optional<T> publicGetService(Class<T> type) {
            return getService(type);
        }
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
    }

    @Nested
    @DisplayName("user principal")
    class UserPrincipalTests {

        @AfterEach
        void clearRequestContext() {
            RequestContextAccessor.remove();
        }

        @Test
        @DisplayName("should return user from current request context")
        void shouldReturnUserFromRequestContext() {
            var user = new UserPrincipal(new javax.security.auth.Subject());
            var context = new RequestContext();
            context.setUser(user);
            RequestContextAccessor.set(context);

            var provider = new StubServiceProvider();
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("should return null when request context is missing")
        void shouldReturnNullWhenRequestContextMissing() {
            var provider = StubServiceProvider.with(UserPrincipal.class, new UserPrincipal(new javax.security.auth.Subject()));
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isNull();
        }

        @Test
        @DisplayName("should return null when request context has no user")
        void shouldReturnNullWhenRequestContextHasNoUser() {
            RequestContextAccessor.set(new RequestContext());

            var provider = new StubServiceProvider();
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isNull();
        }
    }
}
