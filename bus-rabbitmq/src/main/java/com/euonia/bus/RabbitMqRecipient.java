package com.euonia.bus;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.serialization.MessageSerializer;
import com.rabbitmq.client.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class RabbitMqRecipient implements AutoCloseable {
    protected final Connection connection;
    protected final RabbitMqBusOptions options;
    protected final MessageSerializer serializer;
    private final List<Consumer<MessageReceivedEvent>> messageReceivedListeners = new ArrayList<>();
    private final List<Consumer<MessageAcknowledgedEvent>> messageAcknowledgedListeners = new ArrayList<>();

    protected RabbitMqRecipient(Connection connection, RabbitMqBusOptions options, MessageSerializer serializer) {
        this.connection = connection;
        this.options = options;
        this.serializer = serializer;
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

    protected abstract void handle(String channel, Object message, MessageContext context);

    abstract void start(String group);

    @Override
    public void close() {
        messageAcknowledgedListeners.clear();
        messageReceivedListeners.clear();
    }
}
