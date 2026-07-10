package com.euonia.http;

/**
 * HTTP 404 Not Found 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ResourceNotFoundException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}
