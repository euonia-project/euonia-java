package com.euonia.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.euonia.application.BaseApplicationService;
import com.euonia.reflection.ServiceProvider;
import com.euonia.reflection.ServiceProviderConfiguration;
import com.euonia.usecase.UseCaseExecutor;

/**
 * 测试 {@link ApplicationConfiguration} 与 {@link ApplicationServiceBeanPostProcessor}。
 */
@DisplayName("ApplicationConfiguration")
class ApplicationConfigurationTest {

    static class TestApplicationService extends BaseApplicationService {
    }

    @Configuration
    @Import({ServiceProviderConfiguration.class, ApplicationConfiguration.class})
    static class TestConfiguration {
        @Bean
        TestApplicationService testApplicationService() {
            return new TestApplicationService();
        }
    }

    @Test
    @DisplayName("should register use case executor")
    void shouldRegisterUseCaseExecutor() {
        try (var context = new AnnotationConfigApplicationContext(TestConfiguration.class)) {
            assertThat(context.getBean(UseCaseExecutor.class)).isNotNull();
        }
    }

    @Test
    @DisplayName("should inject lazy service provider into application service")
    void shouldInjectLazyServiceProvider() {
        try (var context = new AnnotationConfigApplicationContext(TestConfiguration.class)) {
            var service = context.getBean(TestApplicationService.class);

            assertThat(service.getLazyServiceProvider()).isNotNull();
            assertThat(service.getLazyServiceProvider().getService(ServiceProvider.class))
                .contains(context.getBean(ServiceProvider.class));
        }
    }
}
