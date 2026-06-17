package com.euonia.bus;

import com.euonia.bus.recipient.Executor;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMemoryRequestRecipient extends InMemoryRecipient implements Executor {

    private static final Logger log = Logger.getLogger(InMemoryRequestRecipient.class.getName());

    private final HandlerContext handler;

    public InMemoryRequestRecipient(HandlerContext handler) {
        this.handler = handler;
    }

    @Override
    protected CompletableFuture<Void> handleAsync(String channel, Object data, MessageContext context, boolean aborted) {
        return handler.handleAsync(channel, data, context)
                      .whenComplete((result, exception) -> {
                          if (exception != null) {
                              log.log(Level.WARNING, exception.getMessage(), exception);
                          }
                          context.complete(data);
                      });
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void close() {

    }
}
