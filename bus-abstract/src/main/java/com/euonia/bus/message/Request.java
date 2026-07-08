package com.euonia.bus.message;

/**
 * 表示可通过总线发送的请求消息。
 * 请求是一种期望收到响应的消息，用于请求-响应通信模式。
 *
 * @param <R> 请求期望的响应类型
 * @author damon(zhaorong@outlook.com)
 */
public interface Request<R> extends Message {
}
