package com.euonia.http;

/**
 * HTTP 504 Gateway Timeout 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class GatewayTimeoutException extends HttpStatusException {

    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public GatewayTimeoutException(String message) {
        super(504, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public GatewayTimeoutException(String message, Throwable cause) {
        super(504, message, cause);
    }

}
