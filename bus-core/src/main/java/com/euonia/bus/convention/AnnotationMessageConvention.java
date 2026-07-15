package com.euonia.bus.convention;

import com.euonia.bus.ChannelRegistrar;
import com.euonia.bus.annotation.Multicast;
import com.euonia.bus.annotation.Request;
import com.euonia.bus.annotation.Unicast;
import com.euonia.bus.exception.ChannelNotRegisterException;
import com.euonia.core.ArgumentNullException;

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

    /**
     * 获取约定的名称。
     *
     * @return 约定名称
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * 判断指定通道是否为单播模式。
     *
     * @param channel 通道名称
     * @return 如果通道为单播模式，返回 true；否则返回 false。
     */
    @Override
    public boolean isUnicast(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Unicast.class) != null;
    }

    /**
     * 判断指定通道是否为多播模式。
     *
     * @param channel 通道名称
     * @return 如果通道为多播模式，返回 true；否则返回 false。
     */
    @Override
    public boolean isMulticast(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Multicast.class) != null;
    }

    /**
     * 判断指定通道是否为请求模式。
     *
     * @param channel 通道名称
     * @return 如果通道为请求模式，返回 true；否则返回 false。
     */
    @Override
    public boolean isRequest(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        var registration = ChannelRegistrar.getRegistration(channel)
                                           .orElseThrow(() -> new ChannelNotRegisterException(channel));
        return registration.getMessageType().getAnnotation(Request.class) != null;
    }
}
