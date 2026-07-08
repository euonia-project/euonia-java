package com.euonia.bus.strategy;

import java.util.function.BiPredicate;

/**
 * 传输策略构建器，提供流式 API 来配置消息传输的出站和入站规则。
 *
 * @author damon(zhaorong@outlook.com)
 */
@SuppressWarnings("UnusedReturnValue")
public interface TransportStrategyBuilder {
    /**
     * 获取构建完成的传输策略。
     *
     * @return 构建完成的 {@link TransportStrategy} 实例
     */
    TransportStrategy getStrategy();

    /**
     * 添加出站消息评估规则。
     *
     * @param predicate 用于评估是否允许出站的断言
     * @return 当前 TransportStrategyBuilder 实例
     */
    TransportStrategyBuilder evaluateOutgoing(BiPredicate<String, Class<?>> predicate);

    /**
     * 添加入站消息评估规则。
     *
     * @param predicate 用于评估是否允许入站的断言
     * @return 当前 TransportStrategyBuilder 实例
     */
    TransportStrategyBuilder evaluateIncoming(BiPredicate<String, Class<?>> predicate);

    /**
     * 添加传输策略实例。
     *
     * @param <S>      TransportStrategy 的类型
     * @param strategy 要添加的策略实例
     * @return 当前 TransportStrategyBuilder 实例
     */
    <S extends TransportStrategy> TransportStrategyBuilder add(S strategy);

    /**
     * 添加传输策略类，将使用默认构造函数实例化。
     *
     * @param <S>          TransportStrategy 的类型
     * @param strategyType 要添加的策略类
     * @return 当前 TransportStrategyBuilder 实例
     */
    <S extends TransportStrategy> TransportStrategyBuilder add(Class<S> strategyType);
}
