package com.euonia.bus.strategy;

import java.util.function.Predicate;

class OverridableTransportStrategy implements TransportStrategy {

    private final TransportStrategy delegate;
    private Predicate<String> outgoingPredicate;
    private Predicate<String> incomingPredicate;

    OverridableTransportStrategy(TransportStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return String.format("Override with (%s)", delegate.getName());
    }

    @Override
    public boolean allowOutgoing(String channel) {
        if (outgoingPredicate == null) {
            return delegate.allowOutgoing(channel);
        } else {
            return outgoingPredicate.test(channel);
        }
    }

    @Override
    public boolean allowIncoming(String channel) {
        if (incomingPredicate == null) {
            return delegate.allowIncoming(channel);
        } else {
            return incomingPredicate.test(channel);
        }
    }

    void setOutgoingPredicate(Predicate<String> outgoingPredicate) {
        this.outgoingPredicate = outgoingPredicate;
    }

    void setIncomingPredicate(Predicate<String> incomingPredicate) {
        this.incomingPredicate = incomingPredicate;
    }
}
