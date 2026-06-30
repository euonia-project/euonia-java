package com.euonia.bus.recipient;

import com.euonia.bus.HandlerRegistration;

import java.util.List;
import java.util.Map;

/**
 * RecipientRegistrar is responsible for registering handler registrations with the recipient system. It provides a method to register a list of handler registrations along with a default transport.
 * <p>
 * This interface abstracts the registration process, allowing different implementations to handle the specifics of how handlers are registered and how the default transport is utilized.
 * <p>
 * Implementations of this interface may interact with various recipient types (e.g., Subscribers, Executors) and may manage the lifecycle of handler registrations, ensuring that they are properly registered and available for message dispatching.
 */
public interface RecipientRegistrar {
    /**
     * Registers the given handler registrations with the recipient system, using the specified default transport.
     *
     * @param registrations    the handler registrations to register
     * @param defaultTransport the default transport to use
     */
    void register(Map<String, List<HandlerRegistration>> registrations, String defaultTransport);
}
