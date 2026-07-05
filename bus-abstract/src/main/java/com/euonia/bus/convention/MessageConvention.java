package com.euonia.bus.convention;

/**
 * A set of conventions for determining if a class represents a request, multicast, or unicast message.
 */
public interface MessageConvention {
    /**
     * Gets the name of the convention. Used for diagnostic purposes.
     *
     * @return the name of the convention
     */
    String getName();

    /**
     * Determines if the given channel is a unicast. A unicast message is a message that is sent to a single recipient.
     *
     * @param channel the name of the channel
     * @return true if the message type is a unicast message, false otherwise
     */
    boolean isUnicast(String channel);

    /**
     * Determines if the given channel is a multicast. A multicast message is a message that is sent to multiple recipients.
     *
     * @param channel the name of the channel
     * @return true if the message type is a multicast message, false otherwise
     */
    boolean isMulticast(String channel);

    /**
     * Determines if the given channel is a request. A request message is a message that expects a response.
     *
     * @param channel the name of the channel
     * @return true if the message type is a request message, false otherwise
     */
    boolean isRequest(String channel);
}
