package com.euonia.bus.options;

import com.euonia.bus.Configurator;
import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.strategy.TransportStrategy;

import java.util.List;

public final class MessageBusOptions {
    private final Configurator configurator;
    private String defaultTransport;

    private boolean isEnablePipelineBehaviors = true;

    public MessageBusOptions(Configurator configurator) {
        this.configurator = configurator;
    }

    public String getDefaultTransport() {
        return defaultTransport;
    }

    public void setDefaultTransport(String defaultTransport) {
        this.defaultTransport = defaultTransport;
    }

    public boolean isEnablePipelineBehaviors() {
        return isEnablePipelineBehaviors;
    }

    public MessageConvention getConvention() {
        return configurator.getConventionBuilder().getConvention();
    }

    public List<String> getStrategyAssignedTypes() {
        return configurator.getStrategyBuilders().keySet().stream().toList();
    }

    public TransportStrategy getStrategy(String transport) {
        var builder = configurator.getStrategyBuilders().get(transport);
        return builder == null ? null : builder.getStrategy();
    }

    public void setEnablePipelineBehaviors(boolean enablePipelineBehaviors) {
        isEnablePipelineBehaviors = enablePipelineBehaviors;
    }
}
