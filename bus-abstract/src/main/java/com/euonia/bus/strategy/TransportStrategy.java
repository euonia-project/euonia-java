package com.euonia.bus.strategy;

/**
 * Defines the contract for a transport strategy, which determines how messages
 * are handled for outgoing and incoming operations.
 */
public interface TransportStrategy {
    /**
     * Gets the name of the transport strategy.
     *
     * @return the name of the transport strategy
     */
    String getName();

    /**
     * Determines if the transport strategy allows outgoing messages on the
     * specified channel.
     *
     * @param channel     the channel name
     * @param messageType the type of the message
     * @return true if the transport strategy allows outgoing messages on the
     *         specified channel, false otherwise
     */
    boolean allowOutgoing(String channel, Class<?> messageType);

    /**
     * Determines if the transport strategy allows incoming messages on the
     * specified channel.
     *
     * @param channel     the channel name
     * @param messageType the type of the message
     * @return true if the transport strategy allows incoming messages on the
     *         specified channel, false otherwise
     */
    boolean allowIncoming(String channel, Class<?> messageType);
}
