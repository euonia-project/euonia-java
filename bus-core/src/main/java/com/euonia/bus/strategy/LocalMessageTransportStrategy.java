package com.euonia.bus.strategy;

import com.euonia.bus.annotation.LocalMessage;

/**
 * 本地消息传输策略，仅允许标注了 {@link LocalMessage} 注解的消息通过。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class LocalMessageTransportStrategy implements TransportStrategy {

    @Override
    public String getName() {
        return "LocalMessageTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        return messageType.getAnnotation(LocalMessage.class) != null;
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        return messageType.getAnnotation(LocalMessage.class) != null;
    }
}
