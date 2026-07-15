package com.euonia.bus;

import java.util.function.BiFunction;

/**
 * LambdaHandler 是一个通用的处理器类，它使用 Java 的 BiFunction 接口来处理消息和上下文。
 * 该类允许用户通过传入一个 BiFunction 来定义处理逻辑，从而实现对不同类型消息的处理。
 *
 * @param <T> 消息类型
 * @param <R> 处理结果类型
 * @author damon(zhaorong@outlook.com)
 */
class LambdaHandler<T, R> {
    /**
     * 处理器函数，它接受一个消息和一个消息上下文，并返回处理结果。
     */
    private final BiFunction<T, MessageContext, R> handler;

    /**
     * 构造一个 LambdaHandler 实例。
     *
     * @param handler 处理器函数，它接受一个消息和一个消息上下文，并返回处理结果
     */
    public LambdaHandler(BiFunction<T, MessageContext, R> handler) {
        this.handler = handler;
    }

    /**
     * 处理消息的方法，它调用传入的处理器函数来处理消息和上下文。
     *
     * @param message 要处理的消息
     * @param context 消息上下文，包含处理消息所需的额外信息
     * @return 处理结果，由处理器函数返回
     */
    public R handle(T message, MessageContext context) {
        return handler.apply(message, context);
    }
}
