package com.euonia.bus.convention;

/**
 * 定义总线系统中消息的约定类型。
 * 此枚举用于指定消息在总线中的路由和处理方式，支持单播、多播和请求-响应等不同的通信模式。
 *
 * @author damon(zhaorong@outlook.com)
 */
public enum MessageConventionType {
    /**
     * 表示没有使用特定的消息约定。可在没有约定适用或约定在运行时动态确定时用作默认值。
     */
    NONE,
    /**
     * 表示消息是单播类型，仅发送给一个接收者。
     */
    UNICAST,
    /**
     * 表示消息是多播类型，发送给多个接收者。
     */
    MULTICAST,
    /**
     * 表示消息是请求-响应类型，期望收到响应。
     */
    REQUEST
}
