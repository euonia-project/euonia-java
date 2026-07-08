package com.euonia.bus;

import com.euonia.bus.consistency.OutboxStore;
import com.euonia.bus.exception.MessageTransportException;
import com.euonia.pipeline.*;
import com.euonia.reflection.ServiceProvider;
import com.euonia.tuple.Duet;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 发件箱处理器，在处理消息前检查 {@link OutboxStore} 是否已处理该消息以确保幂等性，
 * 并对失败消息进行定时重试。
 * <p>
 * 用法：
 * <pre>{@code
 * var publisher = new OutboxPublisher(provider);
 * publisher.start();
 * }</pre>
 */
final class OutboxPublisher {
    private static final Logger LOGGER = Logger.getLogger(OutboxPublisher.class.getName());
    private final OutboxStore outboxStore;
    private final ScheduledExecutorService scheduler;
    private final ServiceProvider provider;
    private final PipelineFactory pipelineFactory;
    private final AtomicInteger runningLock = new AtomicInteger(0);
    private final ConcurrentMap<String, Transport> transports = new ConcurrentHashMap<>();

    OutboxPublisher(ServiceProvider provider) {
        this.provider = provider;
        this.outboxStore = provider.getService(OutboxStore.class).orElse(null);
        this.pipelineFactory = provider.getService(PipelineFactory.class)
                                       .orElse(new DefaultPipelineFactory(provider));
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "euonia-inbox-handler");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 启动发件箱处理器，定时轮询 {@link OutboxStore} 并重试发送失败的消息。
     * <p>
     * 如果 {@link OutboxStore} 未配置，则不会启动轮询任务。
     * 轮询间隔为 60 秒。
     */
    void start() {
        if (outboxStore == null) {
            return;
        }
        scheduler.scheduleWithFixedDelay(this::retryAll, 0, 60, TimeUnit.SECONDS);
        LOGGER.info(() -> "OutboxPublisher started with interval 60 SECONDS");
    }

    void retryAll() {
        if (runningLock.get() > 0) {
            return;
        }
        runningLock.incrementAndGet();
        try {
            var items = outboxStore.getFailedMessages();

            var tasks = new ArrayList<CompletableFuture<Void>>();

            for (var item : items) {
                var entry = outboxStore.getAndCache(item.getMessageId());
                if (entry == null) {
                    continue;
                }
                tasks.add(publishAsync(item.getTransport(), entry.getContent()));
            }

            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new))
                             .whenComplete((v, ex) -> {
                                 if (ex != null) {
                                     LOGGER.log(Level.SEVERE, "Error occurred while retrying messages", ex);
                                 }
                                 outboxStore.clearCache();
                             });

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error occurred while retrying messages", ex);
        } finally {
            runningLock.decrementAndGet();
        }
    }

    CompletableFuture<Void> publishAsync(String name, MessageEnvelope<?> message) {
        LOGGER.info(() -> String.format("Message '%s'(%s) transport via %s on channel: %s", message.getMessageId(), message.getClass().getName(), name, message.getChannel()));
        var transport = transports.computeIfAbsent(name, k -> provider.getService(Transport.class).orElseThrow(() -> new MessageTransportException("Transport service not found: " + k)));

        Pipeline<Duet<String, MessageEnvelope<?>>, Void> pipeline = pipelineFactory.create();

        if (outboxStore != null) {
            pipeline.use(OutboxCompletionBehavior.class);
        }

        return pipeline.runAsync(new Duet<>(name, message), ctx -> transport.publishAsync(ctx.value2()))
                       .toCompletableFuture();
    }

    class OutboxCompletionBehavior implements PipelineBehavior<Duet<String, MessageEnvelope<?>>, Void> {

        @Override
        public CompletionStage<Void> handleAsync(Duet<String, MessageEnvelope<?>> context, PipelineDelegate<Duet<String, MessageEnvelope<?>>, Void> next) {
            return next.invoke(context).whenComplete((message, throwable) -> {
                assert outboxStore != null;
                if (throwable != null) {
                    outboxStore.markAsFailed(context.value2().getMessageId(), context.value1(), throwable.getMessage());
                } else {
                    outboxStore.markAsSuccess(context.value2().getMessageId(), context.value1());
                }
            });
        }
    }
}
