package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;

import com.euonia.bus.Bus;
import com.euonia.bus.options.PublishOptions;

/**
 * 发布/订阅模式的 Builder。
 * <p>
 * 用法：
 * <pre>{@code
 * bus.publish(event)
 *    .withChannel("orders")
 *    .withBehavior(b -> b.use(LoggingStep.class))
 *    .withOptions(new PublishOptions())
 *    .executeAsync();
 * }</pre>
 *
 * @param <T> 消息负载类型
 * @author damon(zhaorong@outlook.com)
 */
public final class PublishBuilder<T> extends AbstractBuilder<PublishBuilder<T>, PublishOptions, T, Void> {

    private final Bus bus;
    private final T message;

    public PublishBuilder(Bus bus, T message) {
        super(new PublishOptions());
        this.bus = bus;
        this.message = message;
    }

    /**
     * 执行发布。
     *
     * @return 在所有传输实例完成发送后完成的 future
     */
    public CompletableFuture<Void> executeAsync() {
        return bus.publishAsync(message, options, behavior);
    }
}
