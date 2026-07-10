package com.euonia.http;

/**
 * HTTP 409 Conflict 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ConflictException extends HttpStatusException {

    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public ConflictException(String message) {
        super(409, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public ConflictException(String message, Throwable cause) {
        super(409, message, cause);
    }

}
