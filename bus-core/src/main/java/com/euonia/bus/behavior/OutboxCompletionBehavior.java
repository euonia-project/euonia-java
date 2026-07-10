package com.euonia.bus.behavior;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.consistency.OutboxStore;
import com.euonia.pipeline.PipelineBehavior;
import com.euonia.pipeline.PipelineDelegate;

import java.util.concurrent.CompletionStage;

/**
 * OutboxCompletionBehavior 是一个管道行为，用于在消息处理完成后更新发件箱状态。
 * <p>
 * 它在消息处理成功时将消息标记为成功，在处理失败时将消息标记为失败，并记录异常信息。
 * <p>
 * 该行为依赖于 {@link OutboxStore} 来持久化消息状态。
 *
 * @param <T> 消息负载类型
 * @author damon(zhaorong@outlook.com)
 */
public final class OutboxCompletionBehavior<T> implements PipelineBehavior<MessageEnvelope<T>, Void> {
    private final OutboxStore outboxStore;
    private final String transportName;

    /**
     * 创建一个新的 OutboxCompletionBehavior 实例。
     *
     * @param outboxStore   用于持久化消息状态的发件箱存储
     * @param transportName 传输名称，用于标识消息的传输方式
     */
    public OutboxCompletionBehavior(OutboxStore outboxStore, String transportName) {
        this.outboxStore = outboxStore;
        this.transportName = transportName;
    }

    @Override
    public CompletionStage<Void> handleAsync(MessageEnvelope<T> context, PipelineDelegate<MessageEnvelope<T>, Void> next) {
        return next.invoke(context).whenComplete((message, throwable) -> {
            assert outboxStore != null;
            if (throwable != null) {
                outboxStore.markAsFailed(context.getMessageId(), transportName, throwable.getMessage());
            } else {
                outboxStore.markAsSuccess(context.getMessageId(), transportName);
            }
        });
    }
}
