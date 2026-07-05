package com.euonia.bus.convention;

import com.euonia.bus.ChannelRegistrar;
import com.euonia.bus.annotation.Multicast;
import com.euonia.bus.annotation.Request;
import com.euonia.bus.annotation.Unicast;
import com.euonia.bus.exception.ChannelNotRegisterException;
import com.euonia.utility.Assert;

/**
 * Evaluate whether a type is a message, command, event, or request by annotation decorated on the type.
 */
public class AnnotationMessageConvention implements MessageConvention {
    private static final String NAME = "Annotation decoration message convention";
    private static final String MESSAGE_TYPE_NULL_ERROR = "messageType cannot be null.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isUnicast(String channel) {
        Assert.notNull(channel, MESSAGE_TYPE_NULL_ERROR);
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Unicast.class) != null;
    }

    @Override
    public boolean isMulticast(String channel) {
        Assert.notNull(channel, MESSAGE_TYPE_NULL_ERROR);
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Multicast.class) != null;
    }

    @Override
    public boolean isRequest(String channel) {
        Assert.notNull(channel, MESSAGE_TYPE_NULL_ERROR);
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Request.class) != null;
    }
}
