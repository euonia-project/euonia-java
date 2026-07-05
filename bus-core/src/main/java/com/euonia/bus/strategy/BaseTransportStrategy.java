package com.euonia.bus.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.euonia.utility.Assert;

/**
 * Represents a composite transport strategy that aggregates multiple {@link TransportStrategy} instances
 * and caches the results of outgoing and incoming channel evaluations using {@link ConcurrentHashMap}.
 * <p>
 * Each channel (outgoing, incoming) has its own dedicated cache backed by {@code ConcurrentHashMap&lt;String, Boolean&gt;}.
 * <p>
 * The composite strategy allows for flexible configuration by enabling the addition of custom transport strategies and the definition of custom predicates for determining outgoing and incoming channels. The default strategy can be overridden to provide custom logic for classifying channels without needing to implement a full {@link TransportStrategy}.
 * <p> This design promotes extensibility and performance by leveraging caching and a modular approach to strategy composition.
 */
public class BaseTransportStrategy implements TransportStrategy {
    private final OverridableTransportStrategy defaultStrategy = new OverridableTransportStrategy(new DefaultTransportStrategy());
    private final List<TransportStrategy> strategies = new ArrayList<>();

    private final StrategyCache outgoingCache = new StrategyCache();
    private final StrategyCache incomingCache = new StrategyCache();

    public BaseTransportStrategy() {
        strategies.add(defaultStrategy);
    }

    @Override
    public String getName() {
        return "Composite transport strategy";
    }

    @Override
    public boolean allowOutgoing(String channel) {
        Assert.notNull(channel, "channel cannot be null.");
        return outgoingCache.apply(channel, ch -> strategies.stream().anyMatch(s -> s.allowOutgoing(ch)));
    }

    @Override
    public boolean allowIncoming(String channel) {
        Assert.notNull(channel, "channel cannot be null.");
        return incomingCache.apply(channel, ch -> strategies.stream().anyMatch(s -> s.allowIncoming(ch)));
    }

    /**
     * Adds one or more transport strategies to the composite strategy.
     * The added strategies will be evaluated in order when determining if a channel is outgoing or incoming.
     *
     * @param strategies the transport strategies to add
     */
    public void add(TransportStrategy... strategies) {
        Assert.notEmpty(strategies, "strategies cannot be null or empty.");
        Collections.addAll(this.strategies, strategies);
        resetCache(); // Reset caches to ensure new strategies take effect
    }

    /**
     * Defines a custom strategy for evaluating outgoing channels.
     *
     * @param strategy a predicate that determines if a channel is considered outgoing
     */
    public void defineOutgoingStrategy(Predicate<String> strategy) {
        Assert.notNull(strategy, "strategy cannot be null.");
        defaultStrategy.setOutgoingPredicate(strategy);
    }

    /**
     * Defines a custom strategy for evaluating incoming channels.
     *
     * @param strategy a predicate that determines if a channel is considered incoming
     */
    public void defineIncomingStrategy(Predicate<String> strategy) {
        Assert.notNull(strategy, "strategy cannot be null.");
        defaultStrategy.setIncomingPredicate(strategy);
    }

    /**
     * Resets the caches for outgoing and incoming channel evaluations.
     */
    private void resetCache() {
        outgoingCache.reset();
        incomingCache.reset();
    }

    /**
     * StrategyCache is a helper class that manages a cache for channel evaluations.
     * It uses a {@link ConcurrentHashMap} to store the results of evaluating whether a channel is outgoing or incoming according to the provided strategy.
     * The cache allows for fast lookups while ensuring thread safety in concurrent environments.
     */
    private static class StrategyCache {
        private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

        boolean apply(String channel, Predicate<String> strategy) {
            return cache.computeIfAbsent(channel, strategy::test);
        }

        void reset() {
            cache.clear();
        }
    }
}
