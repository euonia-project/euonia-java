package com.euonia.bus.exception;

/**
 * 消息传输异常，表示总线系统中消息传输过程中发生的异常。
 * <p>
 * 该异常在消息发送或接收出现问题时抛出，如网络错误、序列化问题或其他与传输相关的问题。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageTransportException extends RuntimeException {
    /**
     * 使用指定的详细错误消息构造异常。
     *
     * @param message 详细错误描述
     */
    public MessageTransportException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细错误消息和原因构造异常。
     *
     * @param message 详细错误描述
     * @param cause   异常的根因
     */
    public MessageTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
