package com.euonia.bus;

import java.util.logging.Logger;

import com.euonia.bus.contract.Message;
import com.euonia.bus.inbox.InboxEntry;
import com.euonia.bus.inbox.InboxStore;

/**
 * 幂等处理器装饰器，在处理消息前检查 {@link InboxStore} 是否已处理该消息。
 * <p>
 * 用法：
 * <pre>{@code
 * var realHandler = new CreateOrderHandler();
 * var idempotentHandler = new IdempotentHandler<>(realHandler, inboxStore);
 *
 * // 注册到总线
 * handlerContext.register(OrderCmd.class, IdempotentHandler.class);
 * }</pre>
 *
 * @param <M> 消息类型
 * @param <R> 响应类型
 * @author damon(zhaorong@outlook.com)
 */
public final class IdempotentHandler<M extends Message, R> implements Handler<M, R> {

    private static final Logger LOGGER = Logger.getLogger(IdempotentHandler.class.getName());

    private final Handler<M, R> delegate;
    private final InboxStore inboxStore;

    public IdempotentHandler(Handler<M, R> delegate, InboxStore inboxStore) {
        this.delegate = delegate;
        this.inboxStore = inboxStore;
    }

    @Override
    public R handle(M message, MessageContext context) {
        var messageId = context.getMessageId();

        if (messageId != null && inboxStore.hasBeenProcessed(messageId)) {
            LOGGER.fine(() -> "Message " + messageId + " already processed (idempotent skip)");
            return null;
        }

        var result = delegate.handle(message, context);

        if (messageId != null) {
            inboxStore.markAsProcessed(new InboxEntry(messageId));
        }

        return result;
    }
}
