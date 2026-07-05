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
    private Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior;
    private final PublishOptions options = new PublishOptions();

    public PublishBuilder(Bus bus, T message) {
        this.bus = bus;
        this.message = message;
    }

    /**
     * 指定消息所属通道。
     */
    public PublishBuilder<T> withChannel(String channel) {
        options.setChannel(channel);
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
     * 配置元数据设置器。
     */
    public PublishBuilder<T> withMetadata(Consumer<MessageMetadata> metadataSetter) {
        options.setMetadataSetter(metadataSetter);
        return this;
    }

    /**
     * 指定消息 ID。
     *
     * @param messageId 消息 ID
     * @return 当前的 PublishBuilder 实例
     */
    public PublishBuilder<T> withMessageId(String messageId) {
        options.setMessageId(messageId);
        return this;
    }

    /**
     * 指定消息优先级，数值越大优先级越高。未指定时，系统会使用默认的优先级。
     *
     * @param priority 消息优先级
     * @return 当前的 PublishBuilder 实例
     */
    public PublishBuilder<T> withPriority(int priority) {
        options.setPriority(priority);
        return this;
    }

    /**
     * 指定消息的超时时间，单位为毫秒。未指定时，系统会使用默认的超时时间。
     *
     * @param timeoutMillis 超时时间，单位为毫秒
     * @return 当前的 PublishBuilder 实例
     */
    public PublishBuilder<T> withTimeout(long timeoutMillis) {
        options.setTimeout(timeoutMillis);
        return this;
    }

    /**
     * 指定消息的延迟发送时间，单位为毫秒。未指定时，消息会立即发送。
     *
     * @param delayMillis 延迟发送时间，单位为毫秒
     * @return 当前的 PublishBuilder 实例
     */
    public PublishBuilder<T> withDelay(long delayMillis) {
        options.setDelay(delayMillis);
        return this;
    }

    /**
     * 执行发布。
     *
     * @return 在所有传输实例完成发送后完成的 future
     */
    public CompletableFuture<Void> runAsync() {
        return bus.publishAsync(message, options, behavior);
    }
}
