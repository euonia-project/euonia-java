package com.euonia.bus.strategy;

import java.util.function.BiPredicate;

public interface TransportStrategyBuilder {
    TransportStrategy getStrategy();

    TransportStrategyBuilder evaluateOutgoing(BiPredicate<String, Class<?>> predicate);

    TransportStrategyBuilder evaluateIncoming(BiPredicate<String, Class<?>> predicate);

    <S extends TransportStrategy> TransportStrategyBuilder add(S strategy);

    <S extends TransportStrategy> TransportStrategyBuilder add(Class<S> strategyType);
}
