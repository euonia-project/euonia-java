package com.euonia.bus;

import java.util.function.BiFunction;

class LambdaHandler<T, R> {
    private final BiFunction<T, MessageContext, R> handler;

    public LambdaHandler(BiFunction<T, MessageContext, R> handler) {
        this.handler = handler;
    }

    public R handle(T message, MessageContext context) {
        return handler.apply(message, context);
    }
}
