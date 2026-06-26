package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.euonia.bus.Bus;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.RoutedMessage;
import com.euonia.bus.message.PipelineMessage;
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
 *    .execute();
 * }</pre>
 *
 * @param <T> 消息负载类型
 * @author damon(zhaorong@outlook.com)
 */
public final class PublishBuilder<T> {

    private final Bus bus;
    private final T message;
    private String channel;
    private Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior;
    private PublishOptions options;
    private Consumer<MessageMetadata> metadataSetter;

    public PublishBuilder(Bus bus, T message) {
        this.bus = bus;
        this.message = message;
    }

    /**
     * 指定消息所属通道。
     */
    public PublishBuilder<T> withChannel(String channel) {
        this.channel = channel;
        return this;
    }

    /**
     * 配置管道行为回调。
     */
    public PublishBuilder<T> withBehavior(Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
        this.behavior = behavior;
        return this;
    }

    /**
     * 指定发布选项。
     */
    public PublishBuilder<T> withOptions(PublishOptions options) {
        this.options = options;
        return this;
    }

    /**
     * 配置元数据设置器。
     */
    public PublishBuilder<T> withMetadata(Consumer<MessageMetadata> metadataSetter) {
        this.metadataSetter = metadataSetter;
        return this;
    }

    /**
     * 执行发布。
     *
     * @return 在所有传输实例完成发送后完成的 future
     */
    public CompletableFuture<Void> execute() {
        var opts = options != null ? options : new PublishOptions();
        if (channel != null) {
            opts.setChannel(channel);
        }
        return bus.publishAsync(message, behavior, opts, metadataSetter);
    }
}
