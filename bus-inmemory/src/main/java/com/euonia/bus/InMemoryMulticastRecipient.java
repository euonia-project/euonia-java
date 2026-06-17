package com.euonia.bus;

import com.euonia.bus.recipient.Subscriber;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMemoryMulticastRecipient extends InMemoryRecipient implements Subscriber {

    private static final Logger log = Logger.getLogger(InMemoryMulticastRecipient.class.getName());

    private final HandlerContext handler;

    public InMemoryMulticastRecipient(HandlerContext handler) {
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
