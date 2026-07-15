package com.euonia.bus.exception;

import com.euonia.utility.Resource;

/**
 * 消息投递异常，当消息投递过程中发生错误时抛出。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageDeliverException extends RuntimeException {

    /**
     * 使用默认错误消息构造异常。
     */
    public MessageDeliverException() {
        this(Resource.getString("exception", "MessageDeliverException.Message"));
    }

    /**
     * 使用指定的错误消息构造异常。
     *
     * @param message 详细错误描述
     */
    public MessageDeliverException(String message) {
        super(message);
    }

    /**
     * 使用指定的错误消息和原因构造异常。
     *
     * @param message 详细错误描述
     * @param cause   异常的根因
     */
    public MessageDeliverException(String message, Throwable cause) {
        super(message, cause);
    }
}
