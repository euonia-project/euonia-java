package com.euonia.http;

/**
 * HTTP 429 Too Many Requests 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class TooManyRequestsException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public TooManyRequestsException(String message) {
        super(429, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public TooManyRequestsException(String message, Throwable cause) {
        super(429, message, cause);
    }
}
