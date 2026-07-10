package com.euonia.http;

/**
 * HTTP 502 Bad Gateway 异常，表示服务器作为网关或代理时从上游服务器收到无效响应。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class BadGatewayException extends HttpStatusException {

    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public BadGatewayException(String message) {
        super(502, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public BadGatewayException(String message, Throwable cause) {
        super(502, message, cause);
    }
}
