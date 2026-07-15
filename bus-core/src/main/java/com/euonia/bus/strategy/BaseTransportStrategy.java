package com.euonia.bus.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import com.euonia.utility.Assert;

/**
 * 组合传输策略，聚合多个 {@link TransportStrategy} 实例，并使用 {@link ConcurrentHashMap} 缓存出入站通道的评估结果。
 * <p>
 * 出入站方向各有专属缓存，基于 {@code ConcurrentHashMap} 实现。
 * <p>
 * 此组合策略通过支持添加自定义传输策略和定义评估出入站通道的自定义断言来实现灵活配置。
 * 默认策略可被覆盖以提供自定义逻辑而无需实现完整的 {@link TransportStrategy}。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class BaseTransportStrategy implements TransportStrategy {
    private final OverridableTransportStrategy defaultStrategy = new OverridableTransportStrategy(new DefaultTransportStrategy());
    private final List<TransportStrategy> strategies = new ArrayList<>();

    private final StrategyCache outgoingCache = new StrategyCache();
    private final StrategyCache incomingCache = new StrategyCache();

    public BaseTransportStrategy() {
        strategies.add(defaultStrategy);
    }

    /**
     * 获取组合传输策略的名称。
     *
     * @return 组合传输策略的名称
     */
    @Override
    public String getName() {
        return "Composite transport strategy";
    }

    /**
     * 判断指定的通道和消息类型是否允许发送出站消息。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @return 如果允许发送出站消息则返回 true，否则返回 false
     */
    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        Assert.notNull(channel, "channel cannot be null.");
        Assert.notNull(messageType, "messageType cannot be null.");
        return outgoingCache.apply(channel, messageType, (ch, mt) -> strategies.stream().anyMatch(s -> s.allowOutgoing(ch, mt)));
    }

    /**
     * 判断指定的通道和消息类型是否允许接收入站消息。
     *
     * @param channel     通道名称
     * @param messageType 消息类型
     * @return 如果允许接收入站消息则返回 true，否则返回 false
     */
    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        Assert.notNull(channel, "channel cannot be null.");
        Assert.notNull(messageType, "messageType cannot be null.");
        return incomingCache.apply(channel, messageType, (ch, mt) -> strategies.stream().anyMatch(s -> s.allowIncoming(ch, mt)));
    }

    /**
     * 添加一个或多个传输策略到组合策略中。
     * 添加的策略将在判断通道方向时按顺序评估。
     *
     * @param strategies 要添加的传输策略
     */
    public void add(TransportStrategy... strategies) {
        Assert.notEmpty(strategies, "strategies cannot be null or empty.");
        Collections.addAll(this.strategies, strategies);
        resetCache(); // 重置缓存以确保新策略生效
    }

    /**
     * 定义评估出站通道的自定义策略。
     *
     * @param strategy 判断通道是否视为出站的断言
     */
    public void defineOutgoingStrategy(BiPredicate<String, Class<?>> strategy) {
        Assert.notNull(strategy, "strategy cannot be null.");
        defaultStrategy.setOutgoingPredicate(strategy);
    }

    /**
     * 定义评估入站通道的自定义策略。
     *
     * @param strategy 判断通道是否视为入站的断言
     */
    public void defineIncomingStrategy(BiPredicate<String, Class<?>> strategy) {
        Assert.notNull(strategy, "strategy cannot be null.");
        defaultStrategy.setIncomingPredicate(strategy);
    }

    /**
     * 重置出入站评估缓存。
     */
    private void resetCache() {
        outgoingCache.reset();
        incomingCache.reset();
    }

    /**
     * {@link StrategyCache} 是管理通道评估缓存的辅助类。
     * 使用 {@link ConcurrentHashMap} 存储评估结果，保证并发环境下的快速查找和线程安全。
     */
    private static class StrategyCache {
        private final ConcurrentHashMap<CacheKey, Boolean> cache = new ConcurrentHashMap<>();

        boolean apply(String channel, Class<?> messageType, BiPredicate<String, Class<?>> strategy) {
            return cache.computeIfAbsent(new CacheKey(channel, messageType), k -> strategy.test(k.channel, k.messageType));
        }

        void reset() {
            cache.clear();
        }
    }

    /**
     * 组合缓存键，包含通道和消息类型。
     */
    private static final class CacheKey {
        final String channel;
        final Class<?> messageType;

        CacheKey(String channel, Class<?> messageType) {
            this.channel = channel;
            this.messageType = messageType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey other)) return false;
            return Objects.equals(channel, other.channel) && Objects.equals(messageType, other.messageType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channel, messageType);
        }
    }
}
