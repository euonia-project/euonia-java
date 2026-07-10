package com.euonia.http;

/**
 * HTTP 状态码异常基类，包含 HTTP 状态码和描述错误的消息。
 * <p>
 * 可用于表示各种 HTTP 错误，如 400、401、403、404、500 等。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class HttpStatusException extends RuntimeException {
    /** HTTP 状态码 */
    private final int statusCode;

    /**
     * 使用指定的状态码和消息构造异常。
     *
     * @param statusCode HTTP 状态码
     * @param message    错误描述
     */
    public HttpStatusException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * 使用指定的状态码、消息和原因构造异常。
     *
     * @param statusCode HTTP 状态码
     * @param message    错误描述
     * @param cause      异常的根因
     */
    public HttpStatusException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * 获取 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    public int getStatusCode() {
        return statusCode;
    }

}
