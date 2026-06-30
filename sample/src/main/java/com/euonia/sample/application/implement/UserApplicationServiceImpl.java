package com.euonia.sample.application.implement;

import com.euonia.application.BaseApplicationService;
import com.euonia.reflection.ServiceProvider;
import com.euonia.sample.application.command.UserCreateCommand;
import com.euonia.sample.application.contract.UserApplicationService;
import com.euonia.sample.application.dto.UserCreateDto;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * The UserApplicationServiceImpl class is a concrete implementation of the UserApplicationService interface.
 * It extends the BaseApplicationService, which provides common functionality for application services, such as resolving other services and accessing user information.
 * This class is annotated with @Component to indicate that it is a Spring-managed bean, and @RequestScope to specify that a new instance of this service should be created for each HTTP request.
 * This allows the service to maintain request-specific state if needed.
 */
@Component
@RequestScope
public class UserApplicationServiceImpl extends BaseApplicationService implements UserApplicationService {
    protected UserApplicationServiceImpl(ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    @Override
    public CompletableFuture<Long> createAsync(UserCreateDto data) {
        var command = new UserCreateCommand();
        command.setName(data.getUsername());

        CompletableFuture<Long> future = new CompletableFuture<>();

        var subscribe = new Flow.Subscriber<Long>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(Long item) {
                future.complete(item);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(0L);
            }
        };

        return bus.send(command, Long.class)
                  .withCallback(subscribe)
                  .runAsync()
                  .thenCompose(result -> future);

//        return bus.sendAsync(command, Long.class, subscribe)
//                  .thenCompose(result -> future);
    }
}
