package com.euonia.bus.strategy;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

public class DefaultTransportStrategyBuilder implements TransportStrategyBuilder {
    private final BaseTransportStrategy strategy = new BaseTransportStrategy();

    @Override
    public TransportStrategy getStrategy() {
        return strategy;
    }

    @Override
    public TransportStrategyBuilder evaluateOutgoing(Predicate<Class<?>> predicate) {
        assert predicate != null : "Predicate cannot be null";
        strategy.defineOutgoingStrategy(predicate);
        return this;
    }

    @Override
    public TransportStrategyBuilder evaluateIncoming(Predicate<Class<?>> predicate) {
        assert predicate != null : "Predicate cannot be null";
        strategy.defineIncomingStrategy(predicate);
        return this;
    }

    @Override
    public <S extends TransportStrategy> TransportStrategyBuilder add(S strategy) {
        assert strategy != null : "Strategy cannot be null";
        this.strategy.add(strategy);
        return this;
    }

    @Override
    public <S extends TransportStrategy> TransportStrategyBuilder add(Class<S> strategyType) {
        assert strategyType != null : "Strategy type cannot be null";
        try {
            S instance = strategyType.getDeclaredConstructor().newInstance();
            this.strategy.add(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to instantiate strategy of type: " + strategyType.getName(), exception);
        }
        return this;
    }
}
