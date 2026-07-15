package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import com.euonia.bus.Bus;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.RoutedMessage;
import com.euonia.bus.options.SendOptions;
import com.euonia.core.ArgumentNullException;
import com.euonia.core.ArgumentOutOfRangeException;
import com.euonia.pipeline.Pipeline;

/**
 * 发送/命令模式的 Builder。
 * <p>
 * 不关心响应时：
 * <pre>{@code
 * bus.send(command)
 *    .withChannel("orders")
 *    .executeAsync();
 * }</pre>
 * <p>
 * 需要异步回调响应时：
 * <pre>{@code
 * bus.send(command, Result.class)
 *    .withCallback(new Flow.Subscriber<>() { ... })
 *    .executeAsync();
 * }</pre>
 *
 * @param <T> 消息负载类型
 * @param <R> 响应类型
 * @author damon(zhaorong@outlook.com)
 */
public final class SendBuilder<T, R> {

    private final Bus bus;
    private final T message;
    private final Class<R> responseType;
    private Flow.Subscriber<R> callback;
    private Consumer<Pipeline<RoutedMessage<T>, R>> behavior;
    private final SendOptions options = new SendOptions();

    public SendBuilder(Bus bus, T message, Class<R> responseType) {
        this.bus = bus;
        this.message = message;
        this.responseType = responseType;
    }

    /**
     * 指定消息所属通道。
     */
    public SendBuilder<T, R> withChannel(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        options.setChannel(channel);
        return this;
    }

    /**
     * 配置响应回调（兼容 Flow.Subscriber）。
     */
    public SendBuilder<T, R> withCallback(Flow.Subscriber<R> callback) {
        ArgumentNullException.throwIfNull(callback, "callback");
        this.callback = callback;
        return this;
    }

    /**
     * 配置管道行为回调。
     */
    public SendBuilder<T, R> withBehavior(Consumer<Pipeline<RoutedMessage<T>, R>> behavior) {
        ArgumentNullException.throwIfNull(behavior, "behavior");
        this.behavior = behavior;
        return this;
    }

    /**
     * 配置元数据设置器。
     */
    public SendBuilder<T, R> withMetadata(Consumer<MessageMetadata> metadataSetter) {
        ArgumentNullException.throwIfNull(metadataSetter, "metadataSetter");
        options.setMetadataSetter(metadataSetter);
        return this;
    }

    /**
     * 指定消息 ID。未指定时，系统会自动生成一个唯一的消息 ID。
     *
     * @param messageId 消息 ID
     * @return 当前的 SendBuilder 实例
     */
    public SendBuilder<T, R> withMessageId(String messageId) {
        ArgumentNullException.throwIfNullOrEmpty(messageId, "messageId");
        options.setMessageId(messageId);
        return this;
    }

    /**
     * 指定消息的关联 ID，此 correlationId 用于在接收到回复消息时判断该回复消息是否与当前请求相关联。
     *
     * @param correlationId 关联 ID
     * @return 当前的 SendBuilder 实例
     */
    public SendBuilder<T, R> withCorrelationId(String correlationId) {
        ArgumentNullException.throwIfNullOrEmpty(correlationId, "correlationId");
        options.setCorrelationId(correlationId);
        return this;
    }

    /**
     * 指定消息优先级，数值越大优先级越高。未指定时，系统会使用默认的优先级。
     *
     * @param priority 消息优先级
     * @return 当前的 SendBuilder 实例
     */
    public SendBuilder<T, R> withPriority(int priority) {
        options.setPriority(priority);
        return this;
    }

    /**
     * 指定消息的超时时间，单位为毫秒。未指定时，系统会使用默认的超时时间。
     *
     * @param timeoutMillis 超时时间，单位为毫秒
     * @return 当前的 SendBuilder 实例
     */
    public SendBuilder<T, R> withTimeout(long timeoutMillis) {
        ArgumentOutOfRangeException.throwIfNegative(timeoutMillis, "timeoutMillis");
        options.setTimeout(timeoutMillis);
        return this;
    }

    /**
     * 指定消息的延迟发送时间，单位为毫秒。未指定时，消息会立即发送。
     *
     * @param delayMillis 延迟发送时间，单位为毫秒
     * @return 当前的 SendBuilder 实例
     */
    public SendBuilder<T, R> withDelay(long delayMillis) {
        ArgumentOutOfRangeException.throwIfNegative(delayMillis, "delayMillis");
        options.setDelay(delayMillis);
        return this;
    }

    /**
     * 执行发送。
     *
     * @return 在消息处理完毕时完成的 future
     */
    public CompletableFuture<Void> executeAsync() {
        return bus.sendAsync(message, responseType, callback, options, behavior);
    }
}
