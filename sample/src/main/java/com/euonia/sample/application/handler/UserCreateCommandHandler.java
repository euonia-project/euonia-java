package com.euonia.sample.application.handler;

import com.euonia.bus.Bus;
import com.euonia.bus.Handler;
import com.euonia.bus.MessageContext;
import com.euonia.factory.ObjectFactory;
import com.euonia.sample.application.command.UserCreateCommand;
import com.euonia.sample.domain.aggregate.User;
import com.euonia.spring.BeanScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanScope.PROTOTYPE)
public class UserCreateCommandHandler implements Handler<UserCreateCommand, Void> {

    private final ObjectFactory factory;
    private final Bus bus;

    public UserCreateCommandHandler(ObjectFactory factory, Bus bus) {
        this.factory = factory;
        this.bus = bus;
    }

    @Override
    public Void handle(UserCreateCommand message, MessageContext context) {
        var user = factory.create(User.class, message.getName() == null ? "" : message.getName());
        try (user) {
            user.onSaved((args) -> {
                var events = ((User) args.getNewObject()).getEvents();
                for (var event : events) {
                    bus.publishAsync(event);
                }
            });
            user.setAge(20);
            user.save(false);
            //return ResponseEntity.ok("User created with name: " + user.getName());
            context.response(user.getId());
        }
        return null;
    }
}
