package com.euonia.http;

/**
 * HTTP 403 Forbidden 异常。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ForbiddenException  extends HttpStatusException {

    /**
     * 使用指定的消息构造异常。
     *
     * @param message 错误描述
     */
    public ForbiddenException(String message) {
        super(403, message);
    }

    /**
     * 使用指定的消息和原因构造异常。
     *
     * @param message 错误描述
     * @param cause   异常的根因
     */
    public ForbiddenException(String message, Throwable cause) {
        super(403, message, cause);
    }

}
