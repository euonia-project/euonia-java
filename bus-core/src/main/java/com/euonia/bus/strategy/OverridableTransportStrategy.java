package com.euonia.bus.strategy;

import java.util.function.BiPredicate;

/**
 * 可覆盖的传输策略，包装一个内部委托 {@link TransportStrategy}，
 * 允许通过断言覆盖其出入站判断逻辑。
 * <p>
 * 如果设置了覆盖断言，则优先使用断言；否则回退到内部委托的判断。
 *
 * @author damon(zhaorong@outlook.com)
 */
class OverridableTransportStrategy implements TransportStrategy {

    /**
     * 内部委托的传输策略
     */
    private final TransportStrategy delegate;
    /**
     * 出站覆盖断言
     */
    private BiPredicate<String, Class<?>> outgoingPredicate;
    /**
     * 入站覆盖断言
     */
    private BiPredicate<String, Class<?>> incomingPredicate;

    OverridableTransportStrategy(TransportStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return String.format("Override with (%s)", delegate.getName());
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        if (outgoingPredicate == null) {
            return delegate.allowOutgoing(channel, messageType);
        } else {
            return outgoingPredicate.test(channel, messageType);
        }
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        if (incomingPredicate == null) {
            return delegate.allowIncoming(channel, messageType);
        } else {
            return incomingPredicate.test(channel, messageType);
        }
    }

    /**
     * 设置出站断言覆盖。
     */
    void setOutgoingPredicate(BiPredicate<String, Class<?>> outgoingPredicate) {
        this.outgoingPredicate = outgoingPredicate;
    }

    /**
     * 设置入站断言覆盖。
     */
    void setIncomingPredicate(BiPredicate<String, Class<?>> incomingPredicate) {
        this.incomingPredicate = incomingPredicate;
    }
}
