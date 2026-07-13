package com.euonia.bus.behavior;

import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import com.euonia.bus.MessageEnvelope;
import com.euonia.pipeline.PipelineBehavior;
import com.euonia.pipeline.PipelineDelegate;

/**
 * OutgoingLoggingBehavior 是一个用于记录出站消息的行为类，实现了 PipelineBehavior 接口。
 * 它在消息通过管道发送之前，记录消息的 ID、类型、传输方式和通道信息，以便进行调试和监控。
 * <p>
 * 使用示例：
 * <pre>{@code
 * Pipeline<RoutedMessage<MyMessage>, MyResponse> pipeline = ...;
 * * pipeline.use(OutgoingLoggingBehavior.class, "MyTransport"));
 * }</pre>
 *
 * @param <T> 消息类型
 * @param <R> 响应类型
 * @author damon(zhaorong@outlook.com)
 */
public final class OutgoingLoggingBehavior<T, R> implements PipelineBehavior<MessageEnvelope<T>, R> {
    private static final Logger LOGGER = Logger.getLogger(OutgoingLoggingBehavior.class.getName());

    private final Class<T> messageType;
    private final String transportName;

    /**
     * 创建一个新的 OutgoingLoggingBehavior 实例。
     *
     * @param messageType   消息类型
     * @param transportName 传输名称
     */
    public OutgoingLoggingBehavior(Class<T> messageType, String transportName) {
        this.messageType = messageType;
        this.transportName = transportName;
    }

    @Override
    public CompletionStage<R> handleAsync(MessageEnvelope<T> context, PipelineDelegate<MessageEnvelope<T>, R> next) {
        LOGGER.info(() -> String.format("Message '%s'(%s) transport via %s on channel: %s", context.getMessageId(), messageType.getName(), transportName, context.getChannel()));
        return next.invoke(context);
    }
}
