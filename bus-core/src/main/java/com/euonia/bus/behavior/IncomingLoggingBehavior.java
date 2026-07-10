package com.euonia.bus.behavior;

import com.euonia.bus.RoutedMessage;
import com.euonia.pipeline.PipelineBehavior;
import com.euonia.pipeline.PipelineDelegate;

import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * IncomingLoggingBehavior 是一个用于记录入站消息的行为类，实现了 PipelineBehavior 接口。
 * 它在消息通过管道接收之前，记录消息的内容，以便进行调试和监控。
 * <p>
 * 使用示例：
 * <pre>{@code
 * Pipeline<RoutedMessage<MyMessage>, MyResponse> pipeline = ...;
 * pipeline.use(IncomingLoggingBehavior.class);
 * }</pre>
 * @param <T>
 * @param <R>
 */
public final class IncomingLoggingBehavior<T, R> implements PipelineBehavior<RoutedMessage<T>, R> {
    private static final Logger LOGGER = Logger.getLogger(IncomingLoggingBehavior.class.getName());

    @Override
    public CompletionStage<R> handleAsync(RoutedMessage<T> context, PipelineDelegate<RoutedMessage<T>, R> next) {
        LOGGER.info("Incoming message: " + context);
        return next.invoke(context);
    }
}
