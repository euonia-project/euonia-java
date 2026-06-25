package com.euonia.sample.application.handler;

import com.euonia.bus.MessageContext;
import com.euonia.bus.annotation.Subscribe;
import com.euonia.sample.domain.event.UserCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedEventHandler {
    @Subscribe("com.euonia.sample.domain.event.UserCreatedEvent")
    public void handleUserCreatedEvent(UserCreatedEvent event, MessageContext context) {
        System.out.printf("UserCreatedEvent: %s, MessageId: %s%n", event.getId(), context.getMessageId());
    }
}
