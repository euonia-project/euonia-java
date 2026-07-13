package com.euonia.http;

/**
 * HTTP 500 Internal Server Error 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class InternalServerErrorException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public InternalServerErrorException(String message) {
        super(500, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public InternalServerErrorException(String message, Throwable cause) {
        super(500, message, cause);
    }
}
