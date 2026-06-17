package com.euonia.bus.message;

import com.euonia.bus.MessageContext;

/**
 * 消息处理器函数式接口，定义处理消息并返回结果的契约。
 * <p>
 * 该接口作为 {@link MessageHandlerFactory} 创建的处理器的实际执行入口，
 * 由框架内部使用，不应由应用程序直接实现。
 *
 * @author damon(zhaorong@outlook)
 */
@FunctionalInterface
public interface MessageHandler {
    /**
     * 处理给定的消息并返回结果。
     *
     * @param message 要处理的消息对象
     * @param context 消息处理上下文
     * @return 处理结果，可以为 {@code null}
     */
    Object handle(Object message, MessageContext context);
}
