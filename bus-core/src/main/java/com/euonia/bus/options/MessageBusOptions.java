package com.euonia.bus.options;

import com.euonia.bus.Configurator;
import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.strategy.TransportStrategy;

import java.util.List;

/**
 * Defines the options for configuring the message bus, including transport
 * strategies, message conventions, and pipeline behaviors.
 * This class is immutable and can be used to create a message bus instance with
 * the specified options.
 *
 * @author damon(zhaorong@outlook)
 */
public final class MessageBusOptions {
    private final Configurator configurator;
    private String defaultTransport;

    private boolean isEnablePipelineBehaviors = true;

    /**
     * Creates a new instance of MessageBusOptions with the specified configurator.
     *
     * @param configurator the configurator used to configure the message bus
     */
    public MessageBusOptions(Configurator configurator) {
        this.configurator = configurator;
    }

    /**
     * Gets the default transport strategy name, which can be used for message
     * routing and categorization.
     *
     * @return the default transport strategy name
     */
    public String getDefaultTransport() {
        return defaultTransport;
    }

    /**
     * Sets the default transport strategy name, which can be used for message
     * routing and categorization.
     *
     * @param defaultTransport the default transport strategy name
     */
    public void setDefaultTransport(String defaultTransport) {
        this.defaultTransport = defaultTransport;
    }

    /**
     * Gets whether to enable pipeline behaviors, which can be used for message
     * processing and handling.
     *
     * @return true if pipeline behaviors are enabled, false otherwise
     */
    public boolean isEnablePipelineBehaviors() {
        return isEnablePipelineBehaviors;
    }

    /**
     * Sets whether to enable pipeline behaviors, which can be used for message
     * processing and handling.
     *
     * @param enablePipelineBehaviors true to enable pipeline behaviors, false
     *                                otherwise
     */
    public void setEnablePipelineBehaviors(boolean enablePipelineBehaviors) {
        isEnablePipelineBehaviors = enablePipelineBehaviors;
    }

    /**
     * Gets the message convention, which can be used for message formatting and
     * validation.
     *
     * @return the message convention
     */
    public MessageConvention getConvention() {
        return configurator.getConventionBuilder().getConvention();
    }

    /**
     * Gets the list of transport strategy names, which can be used for message
     * routing and categorization.
     *
     * @return the list of transport strategy names
     */
    public List<String> getStrategyAssignedTypes() {
        return configurator.getStrategyBuilders().keySet().stream().toList();
    }

    /**
     * Gets the transport strategy for the specified transport name, which can be
     * used for message routing and categorization.
     *
     * @param transport the transport name
     * @return the transport strategy for the specified transport name, or null if
     *         not found
     */
    public TransportStrategy getStrategy(String transport) {
        var builder = configurator.getStrategyBuilders().get(transport);
        return builder == null ? null : builder.getStrategy();
    }
}
