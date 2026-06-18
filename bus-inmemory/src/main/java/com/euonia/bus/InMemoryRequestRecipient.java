package com.euonia.bus;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.recipient.Executor;

/**
 * 内存消息总线的请求-响应消息接收者。
 * <p>
 * 继承自 {@link InMemoryRecipient}，实现 {@link Executor} 接口，
 * 用于处理请求类型的消息。通过 {@link HandlerContext} 将消息分发给实际的消息处理器，并在处理完成后将任何异常记录到日志中（不会中断消息处理）。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class InMemoryRequestRecipient extends InMemoryRecipient implements Executor {

    /**
     * 日志记录器。
     */
    private static final Logger log = Logger.getLogger(InMemoryRequestRecipient.class.getName());

    /**
     * 处理器上下文，用于将消息分发给实际的消息处理器。
     */
    private final HandlerContext handler;

    /**
     * 创建请求接收者实例。
     *
     * @param handler 处理器上下文
     */
    public InMemoryRequestRecipient(HandlerContext handler) {
        this.handler = handler;
    }

    /**
     * 异步处理接收到的请求消息。
     * <p>
     * 将消息通过 {@link HandlerContext} 分发给实际的消息处理器。
     * 如果处理过程中发生异常，会记录警告日志但不会中断处理流程。
     *
     * @param channel 消息通道
     * @param message 消息体
     * @param context 消息上下文
     * @param aborted 消息是否已被中止
     * @return 表示异步处理完成的 {@link CompletableFuture}
     */
    @Override
    protected CompletableFuture<Void> handleAsync(String channel, Object message, MessageContext context, boolean aborted) {
        return handler.handleAsync(channel, message, context)
                      .whenComplete((result, exception) -> {
                          if (exception != null) {
                              log.log(Level.WARNING, exception.getMessage(), exception);
                          }
                          context.complete(message);
                      });
    }

    /**
     * 获取接收者名称。
     *
     * @return 接收者的简单类名
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 关闭接收者，释放资源。
     * <p>
     * 当前实现为空，资源释放由父类或其他机制处理。
     */
    @Override
    public void close() {

    }
}
