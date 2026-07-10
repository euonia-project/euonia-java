package com.euonia.http;
/**
 * HTTP 408 Request Timeout 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RequestTimeoutException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public RequestTimeoutException(String message) {
        super(408, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public RequestTimeoutException(String message, Throwable cause) {
        super(408, message, cause);
    }
}
