package com.euonia.bus.strategy;

import java.util.function.Predicate;

public interface TransportStrategyBuilder {
    TransportStrategy getStrategy();

    TransportStrategyBuilder evaluateOutgoing(Predicate<String> predicate);

    TransportStrategyBuilder evaluateIncoming(Predicate<String> predicate);

    <S extends TransportStrategy> TransportStrategyBuilder add(S strategy);

    <S extends TransportStrategy> TransportStrategyBuilder add(Class<S> strategyType);
}
