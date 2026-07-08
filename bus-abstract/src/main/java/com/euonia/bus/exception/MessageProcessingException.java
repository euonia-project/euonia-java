package com.euonia.bus.exception;

/**
 * 消息处理异常，表示总线系统中消息处理过程中发生的异常。
 * <p>
 * 该异常在消息处理或操作出现问题时抛出，如验证错误、业务逻辑错误或其他与处理相关的问题。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageProcessingException extends RuntimeException {

    /**
     * 构造没有详细错误消息的异常。
     */
    public MessageProcessingException() {
        super();
    }

    /**
     * 使用指定的详细错误消息构造异常。
     *
     * @param message 详细错误描述
     */
    public MessageProcessingException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细错误消息和原因构造异常。
     *
     * @param message 详细错误描述
     * @param cause   异常的根因
     */
    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
