package com.euonia.bus.strategy;

import java.util.function.Predicate;

public interface TransportStrategyBuilder {
    TransportStrategy getStrategy();

    TransportStrategyBuilder evaluateOutgoing(Predicate<Class<?>> predicate);

    TransportStrategyBuilder evaluateIncoming(Predicate<Class<?>> predicate);

    <S extends TransportStrategy> TransportStrategyBuilder add(S strategy);

    <S extends TransportStrategy> TransportStrategyBuilder add(Class<S> strategyType);
}
