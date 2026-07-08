package com.euonia.bus.strategy;

import com.euonia.bus.annotation.DistributedMessage;

/**
 * 分布式消息传输策略，仅允许标注了 {@link DistributedMessage} 注解的消息通过。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DistributedMessageTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "DistributedMessageTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        return messageType.getAnnotation(DistributedMessage.class) != null;
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        return messageType.getAnnotation(DistributedMessage.class) != null;
    }
}
