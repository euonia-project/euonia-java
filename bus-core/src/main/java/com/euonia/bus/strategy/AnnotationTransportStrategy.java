package com.euonia.bus.strategy;

import com.euonia.bus.annotation.DispatchIn;
import com.euonia.bus.annotation.ReceiveIn;

/**
 * 基于注解的传输策略实现，通过 {@link DispatchIn} 和 {@link ReceiveIn} 注解判断消息的出入站规则。
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

    /**
     * 获取策略的名称。
     *
     * @return 策略的名称
     */
    @Override
    public String getName() {
        return "AnnotationTransportStrategy";
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
        var annotation = messageType.getAnnotation(DispatchIn.class);
        return annotation != null && annotation.transports().length > 0 && containsRequired(annotation.transports());
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
        var annotation = messageType.getAnnotation(ReceiveIn.class);
        return annotation != null && annotation.transports().length > 0 && containsRequired(annotation.transports());
    }

    /**
     * 判断指定的通道和消息类型是否包含要求的传输名称。
     *
     * @param values 通道的传输名称数组
     * @return 如果包含要求的传输名称则返回 true，否则返回 false
     */
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
