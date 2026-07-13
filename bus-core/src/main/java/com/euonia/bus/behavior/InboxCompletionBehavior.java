package com.euonia.bus.behavior;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.consistency.InboxStore;
import com.euonia.pipeline.PipelineBehavior;
import com.euonia.pipeline.PipelineDelegate;

import java.util.concurrent.CompletionStage;

/**
 * InboxCompletionBehavior 是一个管道行为，用于在消息处理完成后更新收件箱状态。
 * <p>
 * 它在消息处理成功时将消息标记为成功，在处理失败时将消息标记为失败，并记录异常信息。
 * <p>
 * 该行为依赖于 {@link InboxStore} 来持久化消息状态。
 *
 * @param <T> 消息负载类型
 * @author damon(zhaorong@outlook.com)
 */
public final class InboxCompletionBehavior<T> implements PipelineBehavior<MessageEnvelope<T>, Object> {
    private final InboxStore inboxStore;
    private final String handlerName;

    /**
     * 创建一个新的 InboxCompletionBehavior 实例。
     *
     * @param inboxStore  用于持久化消息状态的收件箱存储
     * @param handlerName 处理器名称，用于标识消息的处理器
     */
    public InboxCompletionBehavior(InboxStore inboxStore, String handlerName) {
        this.inboxStore = inboxStore;
        this.handlerName = handlerName;
    }

    @Override
    public CompletionStage<Object> handleAsync(MessageEnvelope<T> context, PipelineDelegate<MessageEnvelope<T>, Object> next) {
        return next.invoke(context).whenComplete((message, throwable) -> {
            if (throwable != null) {
                inboxStore.markAsFailed(context.getMessageId(), handlerName, throwable.getMessage());
            } else {
                inboxStore.markAsSuccess(context.getMessageId(), handlerName);
            }
        });
    }
}
