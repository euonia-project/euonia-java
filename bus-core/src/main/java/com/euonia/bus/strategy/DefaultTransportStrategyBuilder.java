package com.euonia.bus.strategy;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiPredicate;

import com.euonia.utility.Assert;

/**
 * {@link TransportStrategyBuilder} 的默认实现，基于 {@link BaseTransportStrategy} 构建传输策略。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultTransportStrategyBuilder implements TransportStrategyBuilder {
    private final BaseTransportStrategy strategy = new BaseTransportStrategy();

    /**
     * 获取构建完成的传输策略。
     *
     * @return 构建完成的 {@link TransportStrategy} 实例
     */
    @Override
    public TransportStrategy getStrategy() {
        return strategy;
    }

    /**
     * 添加出站消息评估规则。
     *
     * @param predicate 用于评估是否允许出站的断言
     * @return 当前 TransportStrategyBuilder 实例
     */
    @Override
    public TransportStrategyBuilder evaluateOutgoing(BiPredicate<String, Class<?>> predicate) {
        Assert.notNull(predicate, "Predicate cannot be null");
        strategy.defineOutgoingStrategy(predicate);
        return this;
    }

    /**
     * 添加入站消息评估规则。
     *
     * @param predicate 用于评估是否允许入站的断言
     * @return 当前 TransportStrategyBuilder 实例
     */
    @Override
    public TransportStrategyBuilder evaluateIncoming(BiPredicate<String, Class<?>> predicate) {
        Assert.notNull(predicate, "Predicate cannot be null");
        strategy.defineIncomingStrategy(predicate);
        return this;
    }

    /**
     * 添加传输策略实例。
     *
     * @param <S>      TransportStrategy 的类型
     * @param strategy 要添加的策略实例
     * @return 当前 TransportStrategyBuilder 实例
     */
    @Override
    public <S extends TransportStrategy> TransportStrategyBuilder add(S strategy) {
        Assert.notNull(strategy, "Strategy cannot be null");
        this.strategy.add(strategy);
        return this;
    }

    /**
     * 添加传输策略类，将使用默认构造函数实例化。
     *
     * @param <S>          TransportStrategy 的类型
     * @param strategyType 要添加的策略类
     * @return 当前 TransportStrategyBuilder 实例
     */
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
