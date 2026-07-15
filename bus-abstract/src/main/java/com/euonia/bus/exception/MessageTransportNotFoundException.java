package com.euonia.bus.exception;

import com.euonia.utility.Resource;

/**
 * 表示消息传输未找到的异常，通常在尝试使用不存在的消息传输时抛出。
 * <p>
 * 该异常继承自 {@link MessageTransportException}，并提供了未找到的消息传输名称，以便于调试和日志记录。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageTransportNotFoundException extends MessageTransportException {
    private final String transportName;

    /**
     * 使用指定的消息传输名称构造异常。
     *
     * @param transportName 未找到的消息传输名称
     */
    public MessageTransportNotFoundException(String transportName) {
        super(Resource.getString("exception", "MessageTransportNotFoundException.Message", transportName));
        this.transportName = transportName;
    }

    /**
     * 获取未找到的消息传输名称。
     *
     * @return 未找到的消息传输名称
     */
    public String getTransportName() {
        return transportName;
    }
}
