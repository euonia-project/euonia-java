package com.euonia.bus;

import com.euonia.bus.message.Message;

/**
 * 定义处理特定类型消息并返回响应的契约。
 *
 * @param <M> 要处理的消息类型
 * @param <R> 处理消息后返回的响应类型
 * @author damon(zhaorong@outlook.com)
 */
public interface Handler<M extends Message, R> {
    /**
     * 处理类型为 M 的消息并返回类型为 R 的响应。
     *
     * @param message 要处理的消息
     * @param context 处理消息的上下文
     * @return 处理消息后的响应
     */
    R handle(M message, MessageContext context);
}
