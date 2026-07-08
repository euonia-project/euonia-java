package com.euonia.bus.strategy;

import com.euonia.bus.annotation.DispatchIn;
import com.euonia.bus.annotation.ReceiveIn;

/**
 * 基于注解的传输策略实现，通过 {@link DispatchIn} 和 {@link ReceiveIn} 注解
 * 判断消息的出入站规则。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class AnnotationTransportStrategy implements TransportStrategy {
    /** 要求的传输名称数组 */
    private final String[] required;

    /**
     * 使用要求的传输名称构造策略。
     *
     * @param required 要求的传输名称
     */
    public AnnotationTransportStrategy(String... required) {
        this.required = required;
    }

    @Override
    public String getName() {
        return "AnnotationTransportStrategy";
    }

    @Override
    public boolean allowOutgoing(String channel, Class<?> messageType) {
        var annotation = messageType.getAnnotation(DispatchIn.class);
        return annotation != null && annotation.transports().length > 0 && containsRequired(annotation.transports());
    }

    @Override
    public boolean allowIncoming(String channel, Class<?> messageType) {
        var annotation = messageType.getAnnotation(ReceiveIn.class);
        return annotation != null && annotation.transports().length > 0 && containsRequired(annotation.transports());
    }

    private boolean containsRequired(String[] values) {
        for (String value : values) {
            for (String req : required) {
                if (value.equals(req)) {
                    return true;
                }
            }
        }
        return false;
    }
}
