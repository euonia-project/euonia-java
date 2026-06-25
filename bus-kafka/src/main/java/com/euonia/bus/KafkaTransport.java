package com.euonia.bus;

import com.euonia.bus.contract.Transport;

import java.util.concurrent.CompletableFuture;

public final class KafkaTransport implements Transport {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public <M> CompletableFuture<Void> publishAsync(RoutedMessage<M> message) {
        return null;
    }

    @Override
    public <M> CompletableFuture<Void> sendAsync(RoutedMessage<M> message) {
        return null;
    }

    @Override
    public <M, R> CompletableFuture<R> sendAsync(RoutedMessage<M> message, Class<R> responseType) {
        return null;
    }

    @Override
    public <M, R> CompletableFuture<R> callAsync(RoutedMessage<M> message, Class<R> responseType) {
        return null;
    }
}
