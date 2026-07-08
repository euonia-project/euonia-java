package com.euonia.bus.convention;

/**
 * 定义用于判断消息类型是否为请求、多播或单播的约定集。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface MessageConvention {
    /**
     * 获取约定的名称，用于诊断目的。
     *
     * @return 约定的名称
     */
    String getName();

    /**
     * 判断给定的通道是否为单播类型。单播消息是发送给单个接收者的消息。
     *
     * @param channel 通道名称
     * @return 如果消息类型是单播消息则返回 true，否则返回 false
     */
    boolean isUnicast(String channel);

    /**
     * 判断给定的通道是否为多播类型。多播消息是发送给多个接收者的消息。
     *
     * @param channel 通道名称
     * @return 如果消息类型是多播消息则返回 true，否则返回 false
     */
    boolean isMulticast(String channel);

    /**
     * 判断给定的通道是否为请求类型。请求消息是期望收到响应的消息。
     *
     * @param channel 通道名称
     * @return 如果消息类型是请求消息则返回 true，否则返回 false
     */
    boolean isRequest(String channel);
}
