package com.euonia.bus.strategy;

import com.euonia.bus.annotation.LocalMessage;

public class LocalMessageTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "LocalMessageTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        return messageType.getAnnotation(LocalMessage.class) != null;
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        return messageType.getAnnotation(LocalMessage.class) != null;
    }
}
