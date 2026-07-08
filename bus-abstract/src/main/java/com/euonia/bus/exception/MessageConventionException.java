package com.euonia.bus.exception;

/**
 * 消息约定异常，当消息类型不符合预期的约定（单播/多播/请求）时抛出。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageConventionException extends RuntimeException {
    /**
     * 使用错误消息构造异常。
     *
     * @param message 错误描述
     */
    public MessageConventionException(String message) {
        super(message);
    }
}
