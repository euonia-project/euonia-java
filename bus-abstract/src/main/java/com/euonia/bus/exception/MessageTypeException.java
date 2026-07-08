package com.euonia.bus.exception;

/**
 * 消息类型异常，当消息类型不符合预期配置时抛出。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageTypeException extends RuntimeException {
    /**
     * 使用指定的错误消息构造异常。
     *
     * @param message 详细错误描述
     */
    public MessageTypeException(String message) {
        super(message);
    }
}
