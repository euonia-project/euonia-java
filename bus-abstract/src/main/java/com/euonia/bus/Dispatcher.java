package com.euonia.bus;

import java.util.List;

/**
 * 定义消息分发器，用于确定消息应被派发到哪些传输通道。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Dispatcher {
    /**
     * 确定给定通道的消息应被派发到哪些传输通道。
     *
     * @param channel     消息的通道
     * @param messageType 消息的类型
     * @return 传输名称列表
     */
    List<String> determine(String channel, Class<?> messageType);
}
