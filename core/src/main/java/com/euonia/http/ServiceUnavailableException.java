package com.euonia.http;

/**
 * HTTP 503 Service Unavailable 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ServiceUnavailableException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public ServiceUnavailableException(String message) {
        super(503, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super(503, message, cause);
    }
}
