package com.euonia.bus.strategy;

public class DefaultTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "DefaultTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel) {
        return false;
    }

    @Override
    public boolean allowIncoming(String channel) {
        return false;
    }
}
