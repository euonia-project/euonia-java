package com.euonia.bus.exception;

/**
 * MessagePersistentException 是一个运行时异常，用于表示消息持久化失败的情况。
 * <p>
 * 当消息在持久化过程中发生错误时，可以抛出此异常以通知调用方。
 */
public class MessagePersistentException extends RuntimeException {
    public MessagePersistentException(String message) {
        super(message);
    }
}
