package com.euonia.bus.strategy;

import com.euonia.bus.annotation.LocalMessage;

public class LocalMessageTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "LocalMessageTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel) {
        try {
            return Class.forName(channel).getAnnotation(LocalMessage.class) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean allowIncoming(String channel) {
        try {
            return Class.forName(channel).getAnnotation(LocalMessage.class) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
