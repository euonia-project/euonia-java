package com.euonia.bus.convention;

import com.euonia.bus.ChannelRegistrar;
import com.euonia.bus.annotation.Multicast;
import com.euonia.bus.annotation.Request;
import com.euonia.bus.annotation.Unicast;
import com.euonia.bus.exception.ChannelNotRegisterException;
import com.euonia.utility.Assert;

/**
 * 基于注解的消息约定实现，通过类型上的注解（{@link Unicast}、{@link Multicast}、{@link Request}）
 * 来判断消息的通信模式。
 * <p>
 * 通过 {@link ChannelRegistrar} 查找通道对应的消息类型，然后检查其上的注解。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class AnnotationMessageConvention implements MessageConvention {
    private static final String NAME = "Annotation decoration message convention";
    private static final String MESSAGE_TYPE_NULL_ERROR = "messageType cannot be null.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isUnicast(String channel) {
        Assert.notNull(channel, MESSAGE_TYPE_NULL_ERROR);
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Unicast.class) != null;
    }

    @Override
    public boolean isMulticast(String channel) {
        Assert.notNull(channel, MESSAGE_TYPE_NULL_ERROR);
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Multicast.class) != null;
    }

    @Override
    public boolean isRequest(String channel) {
        Assert.notNull(channel, MESSAGE_TYPE_NULL_ERROR);
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Request.class) != null;
    }
}
