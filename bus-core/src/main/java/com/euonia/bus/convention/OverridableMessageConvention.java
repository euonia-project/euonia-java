package com.euonia.bus.convention;

import java.util.function.Predicate;

class OverridableMessageConvention implements MessageConvention {
    private final MessageConvention innerConvention;
    private Predicate<String> unicastPredicate;
    private Predicate<String> multicastPredicate;
    private Predicate<String> requestPredicate;

    OverridableMessageConvention(MessageConvention innerConvention) {
        this.innerConvention = innerConvention;
    }

    @Override
    public String getName() {
        return String.format("Override with %s", innerConvention.getName());
    }

    @Override
    public boolean isUnicast(String channel) {
        if (unicastPredicate == null) {
            return innerConvention.isUnicast(channel);
        } else {
            return unicastPredicate.test(channel);
        }
    }

    @Override
    public boolean isMulticast(String channel) {
        if (multicastPredicate == null) {
            return innerConvention.isMulticast(channel);
        } else {
            return multicastPredicate.test(channel);
        }
    }

    @Override
    public boolean isRequest(String channel) {
        if (requestPredicate == null) {
            return innerConvention.isRequest(channel);
        } else {
            return requestPredicate.test(channel);
        }
    }

    public void setUnicastPredicate(Predicate<String> unicastPredicate) {
        this.unicastPredicate = unicastPredicate;
    }

    public void setMulticastPredicate(Predicate<String> multicastPredicate) {
        this.multicastPredicate = multicastPredicate;
    }

    public void setRequestPredicate(Predicate<String> requestPredicate) {
        this.requestPredicate = requestPredicate;
    }
}
