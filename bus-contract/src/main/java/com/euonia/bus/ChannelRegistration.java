package com.euonia.bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChannelRegistration represents the registration of a message handler for a
 * specific channel and message type.
 */
public class ChannelRegistration {
    private final Class<?> messageType;

    private final List<ChannelHandler> handlers = new ArrayList<>();

    public ChannelRegistration(Class<?> messageType) {
        this.messageType = messageType;
    }

    public Class<?> getMessageType() {
        return messageType;
    }

    public List<ChannelHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    public ChannelRegistration addHandler(ChannelHandler handler) {
        this.handlers.add(handler);
        return this;
    }
}
