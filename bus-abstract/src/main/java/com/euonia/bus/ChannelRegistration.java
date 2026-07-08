package com.euonia.bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ChannelRegistration} 表示特定通道和消息类型的处理器注册信息。
 * <p>
 * 包含消息类型和对应的处理器列表，支持通过 {@link #addHandler(ChannelHandler)}
 * 方法以流式方式添加处理器。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ChannelRegistration {
    /**
     * 消息类型
     */
    private final Class<?> messageType;

    /**
     * 处理器列表
     */
    private final List<ChannelHandler> handlers = new ArrayList<>();

    /**
     * 使用指定的消息类型构造通道注册信息。
     *
     * @param messageType 消息类型
     */
    public ChannelRegistration(Class<?> messageType) {
        this.messageType = messageType;
    }

    /**
     * 获取消息类型。
     *
     * @return 消息类型
     */
    public Class<?> getMessageType() {
        return messageType;
    }

    /**
     * 获取处理器列表的不可修改视图。
     *
     * @return 处理器列表
     */
    public List<ChannelHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    /**
     * 添加处理器并返回当前实例以支持链式调用。
     *
     * @param handler 要添加的处理器
     * @return 当前 ChannelRegistration 实例
     */
    public ChannelRegistration addHandler(ChannelHandler handler) {
        this.handlers.add(handler);
        return this;
    }
}
