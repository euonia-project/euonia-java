package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.euonia.bus.Bus;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.RoutedMessage;
import com.euonia.bus.message.Request;
import com.euonia.bus.options.CallOptions;
import com.euonia.pipeline.Pipeline;
import com.euonia.utility.Assert;

/**
 * 请求/响应模式的 Builder。
 * <p>
 * 用法：
 * <pre>{@code
 * Result result = bus.call(request, Result.class)
 *                    .withChannel("orders")
 *                    .withBehavior(b -> b.use(AuthStep.class))
 *                    .withOptions(new CallOptions())
 *                    .executeAsync()
 *                    .get();
 * }</pre>
 *
 * @param <T> 请求类型，必须实现 {@link Request}
 * @param <R> 响应类型
 * @author damon(zhaorong@outlook.com)
 */
public final class CallBuilder<T, R> {

    private final Bus bus;
    private final T request;
    private final Class<R> responseType;
    private Consumer<Pipeline<RoutedMessage<T>, R>> behavior;
    private final CallOptions options = new CallOptions();

    public CallBuilder(Bus bus, T request, Class<R> responseType) {
        this.bus = bus;
        this.request = request;
        this.responseType = responseType;
    }

    /**
     * 指定消息所属通道。
     */
    public CallBuilder<T, R> withChannel(String channel) {
        Assert.notNull(channel, "channel must not be null");
        options.setChannel(channel);
        return this;
    }

    /**
     * 配置管道行为回调。
     */
    public CallBuilder<T, R> withBehavior(Consumer<Pipeline<RoutedMessage<T>, R>> behavior) {
        Assert.notNull(behavior, "behavior is null");
        this.behavior = behavior;
        return this;
    }

    /**
     * 配置元数据设置器。
     */
    public CallBuilder<T, R> withMetadata(Consumer<MessageMetadata> metadataSetter) {
        Assert.notNull(metadataSetter, "metadataSetter must not be null");
        options.setMetadataSetter(metadataSetter);
        return this;
    }

    /**
     * 指定请求的 messageId。
     *
     * @param messageId 请求的 messageId
     * @return 当前的 CallBuilder 实例
     */
    public CallBuilder<T, R> withMessageId(String messageId) {
        Assert.notEmpty(messageId, "messageId must not be empty");
        options.setMessageId(messageId);
        return this;
    }

    /**
     * 指定消息的关联 ID，此 correlationId 用于在接收到回复消息时判断该回复消息是否与当前请求相关联。
     *
     * @param correlationId 请求的 correlationId
     * @return 当前的 CallBuilder 实例
     */
    public CallBuilder<T, R> withCorrelationId(String correlationId) {
        Assert.notEmpty(correlationId, "correlationId must not be empty");
        options.setCorrelationId(correlationId);
        return this;
    }

    /**
     * 指定消息优先级，数值越大优先级越高。未指定时，系统会使用默认的优先级。
     *
     * @param priority 请求的优先级
     * @return 当前的 CallBuilder 实例
     */
    public CallBuilder<T, R> withPriority(int priority) {
        options.setPriority(priority);
        return this;
    }

    /**
     * 指定消息的超时时间，单位为毫秒。未指定时，系统会使用默认的超时时间。
     *
     * @param timeoutMillis 超时时间，单位为毫秒
     * @return 当前的 CallBuilder 实例
     */
    public CallBuilder<T, R> withTimeout(long timeoutMillis) {
        options.setTimeout(timeoutMillis);
        return this;
    }

    /**
     * 指定消息的延迟发送时间，单位为毫秒。未指定时，消息会立即发送。
     *
     * @param delayMillis 延迟发送时间，单位为毫秒
     * @return 当前的 CallBuilder 实例
     */
    public CallBuilder<T, R> withDelay(long delayMillis) {
        options.setDelay(delayMillis);
        return this;
    }

    /**
     * 执行请求。
     *
     * @return 在收到响应时完成并携带响应结果的 future
     */
    public CompletableFuture<R> executeAsync() {
        return bus.callAsync(request, responseType, options, behavior);
    }
}
