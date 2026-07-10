package com.euonia.http;

/**
 * HTTP 400 Bad Request 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class BadRequestException extends HttpStatusException {

    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public BadRequestException(String message) {
        super(400, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public BadRequestException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
