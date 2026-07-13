package com.euonia.bus.behavior;

import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import com.euonia.bus.MessageEnvelope;
import com.euonia.pipeline.PipelineBehavior;
import com.euonia.pipeline.PipelineDelegate;

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
public final class IncomingLoggingBehavior<T, R> implements PipelineBehavior<MessageEnvelope<T>, R> {
    private static final Logger LOGGER = Logger.getLogger(IncomingLoggingBehavior.class.getName());

    @Override
    public CompletionStage<R> handleAsync(MessageEnvelope<T> context, PipelineDelegate<MessageEnvelope<T>, R> next) {
        LOGGER.info(()-> String.format("Message '%s'(%s) is received on channel: %s", context.getMessageId(), context.getTypeName(), context.getChannel()));
        return next.invoke(context);
    }
}
