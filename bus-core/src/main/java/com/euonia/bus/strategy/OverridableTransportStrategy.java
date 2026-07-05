package com.euonia.bus.strategy;

import java.util.function.BiPredicate;

class OverridableTransportStrategy implements TransportStrategy {

    private final TransportStrategy delegate;
    private BiPredicate<String, Class<?>> outgoingPredicate;
    private BiPredicate<String, Class<?>> incomingPredicate;

    OverridableTransportStrategy(TransportStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return String.format("Override with (%s)", delegate.getName());
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        if (outgoingPredicate == null) {
            return delegate.allowOutgoing(channel, messageType);
        } else {
            return outgoingPredicate.test(channel, messageType);
        }
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        if (incomingPredicate == null) {
            return delegate.allowIncoming(channel, messageType);
        } else {
            return incomingPredicate.test(channel, messageType);
        }
    }

    void setOutgoingPredicate(BiPredicate<String, Class<?>> outgoingPredicate) {
        this.outgoingPredicate = outgoingPredicate;
    }

    void setIncomingPredicate(BiPredicate<String, Class<?>> incomingPredicate) {
        this.incomingPredicate = incomingPredicate;
    }
}
