package com.euonia.bus;

import com.euonia.bus.recipient.Executor;
import com.euonia.bus.serialization.MessageSerializer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public final class RabbitMqRequestExecutor extends RabbitMqRecipient implements Executor {

    private Channel channel;

    private final Class<? extends RoutedMessage<?>> messageType;

    private final HandlerContext handler;

    public RabbitMqRequestExecutor(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<? extends RoutedMessage<?>> messageType) {
        super(connection, options, serializer);
        this.handler = handler;
        this.messageType = messageType;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    protected void handle(String channel, Object message, MessageContext context) {

    }

    @Override
    void start(String group) {

    }
}
