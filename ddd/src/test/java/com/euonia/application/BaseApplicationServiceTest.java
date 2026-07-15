package com.euonia.application;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

        @Test
        @DisplayName("should return user when registered")
        void shouldReturnUserWhenRegistered() {
            var user = new UserPrincipal(new javax.security.auth.Subject());
            var provider = StubServiceProvider.with(UserPrincipal.class, user);
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("should return null when user not registered")
        void shouldReturnNullWhenUserNotRegistered() {
            var provider = new StubServiceProvider();
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isNull();
        }
    }
}
