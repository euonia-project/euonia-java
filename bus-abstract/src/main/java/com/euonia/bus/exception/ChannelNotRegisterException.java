package com.euonia.bus.exception;

import com.euonia.utility.Resource;

/**
 * 表示消息通道未注册的异常。
 * <p>
 * 当尝试在未注册的消息通道上发送或接收消息时，将抛出此异常。它包含未注册的通道名称，以便于调试和日志记录。
 */
public class ChannelNotRegisterException extends RuntimeException {

    private final String channel;

    /**
     * 构造一个新的 {@code ChannelNotRegisterException}，并指定未注册的通道名称。
     *
     * @param channel 未注册的消息通道名称
     */
    public ChannelNotRegisterException(String channel) {
        super(Resource.getString("resource", "ChannelNotRegisterException.Message", channel));
        this.channel = channel;
    }

    /**
     * 构造一个新的 {@code ChannelNotRegisterException}，并指定未注册的通道名称和详细消息。
     *
     * @param channel 未注册的消息通道名称
     * @param message 详细的异常消息
     */
    public ChannelNotRegisterException(String channel, String message) {
        super(message);
        this.channel = channel;
    }

    /**
     * 获取未注册的消息通道名称。
     *
     * @return 未注册的消息通道名称
     */
    public String getChannel() {
        return channel;
    }
}
