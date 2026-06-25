package com.euonia.bus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.serialization.MessageSerializer;
import com.rabbitmq.client.Connection;

public abstract class RabbitMqRecipient {
    protected final Connection connection;
    protected final RabbitMqBusOptions options;
    protected final HandlerContext handler;
    protected final MessageSerializer serializer;
    protected final Type messageType;
    private final List<Consumer<MessageReceivedEvent>> messageReceivedListeners = new ArrayList<>();
    private final List<Consumer<MessageAcknowledgedEvent>> messageAcknowledgedListeners = new ArrayList<>();

    protected RabbitMqRecipient(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Type messageType) {
        this.connection = connection;
        this.options = options;
        this.handler = handler;
        this.serializer = serializer;
        this.messageType = messageType;
    }

    public void onMessageReceived(Consumer<MessageReceivedEvent> listener) {
        messageReceivedListeners.add(listener);
    }

    public void onMessageAcknowledged(Consumer<MessageAcknowledgedEvent> listener) {
        messageAcknowledgedListeners.add(listener);
    }

    protected void raiseMessageReceived(MessageReceivedEvent event) {
        for (Consumer<MessageReceivedEvent> listener : messageReceivedListeners) {
            listener.accept(event);
        }
    }

    protected void raiseMessageAcknowledged(MessageAcknowledgedEvent event) {
        for (Consumer<MessageAcknowledgedEvent> listener : messageAcknowledgedListeners) {
            listener.accept(event);
        }
    }

    protected CompletableFuture<Object> handleAsync(RoutedMessage<?> message, MessageContext context) {
        return handler.handleAsync(message.getPayload(), context)
                      .whenComplete((result, error) -> {
                          if (error != null) {
                              context.failure(error);
                          } else {
                              context.response(result);
                          }
                      });
    }

    abstract void start(String group);

    public String getName() {
        return getClass().getSimpleName();
    }
}
