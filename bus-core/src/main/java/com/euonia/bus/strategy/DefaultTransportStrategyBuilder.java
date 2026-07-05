package com.euonia.bus.strategy;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiPredicate;

import com.euonia.utility.Assert;

public class DefaultTransportStrategyBuilder implements TransportStrategyBuilder {
    private final BaseTransportStrategy strategy = new BaseTransportStrategy();

    @Override
    public TransportStrategy getStrategy() {
        return strategy;
    }

    @Override
    public TransportStrategyBuilder evaluateOutgoing(BiPredicate<String, Class<?>> predicate) {
        Assert.notNull(predicate, "Predicate cannot be null");
        strategy.defineOutgoingStrategy(predicate);
        return this;
    }

    @Override
    public TransportStrategyBuilder evaluateIncoming(BiPredicate<String, Class<?>> predicate) {
        Assert.notNull(predicate, "Predicate cannot be null");
        strategy.defineIncomingStrategy(predicate);
        return this;
    }

    @Override
    public <S extends TransportStrategy> TransportStrategyBuilder add(S strategy) {
        Assert.notNull(strategy, "Strategy cannot be null");
        this.strategy.add(strategy);
        return this;
    }

    @Override
    public <S extends TransportStrategy> TransportStrategyBuilder add(Class<S> strategyType) {
        Assert.notNull(strategyType, "Strategy type cannot be null");
        try {
            S instance = strategyType.getDeclaredConstructor().newInstance();
            this.strategy.add(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to instantiate strategy of type: " + strategyType.getName(), exception);
        }
        return this;
    }
}
