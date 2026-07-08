package com.euonia.application;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.reflection.DelegateServiceProvider;
import com.euonia.security.UserPrincipal;

/**
 * 测试 {@link BaseApplicationService}。
 */
@SuppressWarnings("unused")
@DisplayName("BaseApplicationService")
class BaseApplicationServiceTest {

    static class TestAppService extends BaseApplicationService {
        TestAppService(com.euonia.reflection.ServiceProvider provider) {
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
            var provider = new DelegateServiceProvider(type ->
                type == String.class ? myService : null);
            var svc = new TestAppService(provider);

            var result = svc.publicGetService(String.class);

            assertThat(result).isPresent().containsSame(myService);
        }

        @Test
        @DisplayName("should return empty for unregistered service")
        void shouldReturnEmptyForUnregisteredService() {
            var provider = new DelegateServiceProvider(type -> null);
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
            var user = new com.euonia.security.UserPrincipal(new javax.security.auth.Subject());
            var provider = new DelegateServiceProvider(type ->
                type == UserPrincipal.class ? user : null);
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("should return null when user not registered")
        void shouldReturnNullWhenUserNotRegistered() {
            var provider = new DelegateServiceProvider(type -> null);
            var svc = new TestAppService(provider);

            assertThat(svc.getUser()).isNull();
        }
    }
}
