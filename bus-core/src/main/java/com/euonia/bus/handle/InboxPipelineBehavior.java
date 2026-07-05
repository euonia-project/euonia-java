package com.euonia.bus.handle;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.inbox.InboxStore;
import com.euonia.pipeline.PipelineBehavior;
import com.euonia.pipeline.PipelineDelegate;
import com.euonia.tuple.Duet;

import java.util.concurrent.CompletionStage;

final class InboxPipelineBehavior implements PipelineBehavior<Duet<String, MessageEnvelope<?>>, Object> {
    private final InboxStore inboxStore;

    InboxPipelineBehavior(InboxStore inboxStore) {
        this.inboxStore = inboxStore;
    }

    @Override
    public CompletionStage<Object> handleAsync(Duet<String, MessageEnvelope<?>> context, PipelineDelegate<Duet<String, MessageEnvelope<?>>, Object> next) {
        return next.invoke(context).whenComplete((message, throwable) -> {
            if (throwable != null) {
                inboxStore.markAsFailed(context.value2().getMessageId(), context.value1(), throwable.getMessage());
            }
            else {
                inboxStore.markAsSuccess(context.value2().getMessageId(), context.value1());
            }
        });
    }
}
