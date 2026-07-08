package com.euonia.bus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.consistency.OutboxEntry;
import com.euonia.bus.consistency.OutboxStore;

/**
 * 投递箱发布器，异地轮询 {@link OutboxStore} 并将消息发送到 {@link Bus}。
 * <p>
 * 用法：
 * <pre>{@code
 * var publisher = new OutboxPublisher(bus, outboxStore, 5, TimeUnit.SECONDS);
 * publisher.start();
 * // 应用关闭时：
 * publisher.shutdown();
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class OutboxPublisher implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(OutboxPublisher.class.getName());

    private final Bus bus;
    private final OutboxStore outboxStore;
    private final ScheduledExecutorService scheduler;
    private final long interval;
    private final TimeUnit intervalUnit;

    public OutboxPublisher(Bus bus, OutboxStore outboxStore, long interval, TimeUnit intervalUnit) {
        this.bus = bus;
        this.outboxStore = outboxStore;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "euonia-outbox-publisher");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this::publishAll, 0, interval, intervalUnit);
        LOGGER.info(() -> "OutboxPublisher started with interval " + interval + " " + intervalUnit);
    }

    private void publishAll() {
        var pending = outboxStore.getPendingMessages();
        if (pending.isEmpty()) {
            return;
        }

        LOGGER.fine(() -> "OutboxPublisher found " + pending.size() + " pending messages");

        for (OutboxEntry msg : pending) {
            try {
                bus.publishAsync(msg.getPayload(), null, null).whenComplete((v, ex) -> {
                    if (ex != null) {
                        LOGGER.log(Level.WARNING, ex, () -> "Failed to publish outbox message " + msg.getId());
                    } else {
                        outboxStore.markAsSent(msg.getId());
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error publishing outbox message " + msg.getId(), e);
            }
        }
    }

    public void flush() {
        publishAll();
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        LOGGER.info("OutboxPublisher shutting down...");
        publishAll();
        scheduler.shutdown();
        try {
            var terminated = scheduler.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated) {
                LOGGER.warning("OutboxPublisher did not terminate within the timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
