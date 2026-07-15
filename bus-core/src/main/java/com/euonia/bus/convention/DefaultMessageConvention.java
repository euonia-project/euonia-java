package com.euonia.bus.convention;

import com.euonia.bus.ChannelRegistrar;
import com.euonia.bus.message.Multicast;
import com.euonia.bus.message.Request;
import com.euonia.bus.message.Unicast;
import com.euonia.core.ArgumentNullException;

/**
 * {@link DefaultMessageConvention} 是 {@link MessageConvention} 接口的简单实现，使用类类型检查来判断消息类型。
 * <p>
 * 它将任何实现了 {@link Unicast}（但不是 Unicast 本身）的类视为单播消息，
 * 将任何实现了 {@link Multicast}（但不是 Multicast 本身）的类视为多播消息，
 * 将任何实现了 {@link Request}（但不是 Request 本身）的类视为请求消息。
 * <p>
 * 此约定简单直接，依赖类层次结构来判断消息类型，易于使用和理解。
 * 适用于消息类型可以通过类结构轻松区分的简单场景。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultMessageConvention implements MessageConvention {
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isUnicast(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        var registration = ChannelRegistrar.getRegistration(channel).orElse(null);
        if (registration == null) {
            return false;
        }
        return Unicast.class.isAssignableFrom(registration.getMessageType()) && registration.getMessageType() != Unicast.class;
    }

    @Override
    public boolean isMulticast(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        var registration = ChannelRegistrar.getRegistration(channel).orElse(null);
        if (registration == null) {
            return false;
        }
        return Multicast.class.isAssignableFrom(registration.getMessageType()) && registration.getMessageType() != Multicast.class;
    }

    @Override
    public boolean isRequest(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        var registration = ChannelRegistrar.getRegistration(channel).orElse(null);
        if (registration == null) {
            return false;
        }
        return Request.class.isAssignableFrom(registration.getMessageType()) && registration.getMessageType() != Request.class;
    }
}
