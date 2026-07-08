package com.euonia.bus.strategy;

/**
 * 默认传输策略，默认拒绝所有出入站消息。
 * 通常作为回退策略使用，由 {@link BaseTransportStrategy} 中的可覆盖策略包装。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "DefaultTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        return false;
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        return false;
    }
}
