package com.euonia.bus.strategy;

import com.euonia.bus.annotation.DistributedMessage;

public class DistributedMessageTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "DistributedMessageTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel) {
        try {
            return Class.forName(channel).getAnnotation(DistributedMessage.class) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean allowIncoming(String channel) {
        try {
            return Class.forName(channel).getAnnotation(DistributedMessage.class) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
