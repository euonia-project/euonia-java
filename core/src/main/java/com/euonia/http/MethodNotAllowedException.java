package com.euonia.http;

/**
 * HTTP 405 Method Not Allowed 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MethodNotAllowedException extends HttpStatusException {
    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public MethodNotAllowedException(String message) {
        super(405, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public MethodNotAllowedException(String message, Throwable cause) {
        super(405, message, cause);
    }
}
