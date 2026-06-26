package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.euonia.bus.Bus;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.RoutedMessage;
import com.euonia.bus.contract.Request;
import com.euonia.bus.message.PipelineMessage;
import com.euonia.bus.options.CallOptions;

/**
 * 请求/响应模式的 Builder。
 * <p>
 * 用法：
 * <pre>{@code
 * Result result = bus.call(request, Result.class)
 *                    .withChannel("orders")
 *                    .withBehavior(b -> b.use(AuthStep.class))
 *                    .withOptions(new CallOptions())
 *                    .execute()
 *                    .get();
 * }</pre>
 *
 * @param <T> 请求类型，必须实现 {@link Request}
 * @param <R> 响应类型
 * @author damon(zhaorong@outlook.com)
 */
public final class CallBuilder<T extends Request<R>, R> {

    private final Bus bus;
    private final T request;
    private final Class<R> responseType;
    private String channel;
    private Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior;
    private CallOptions options;
    private Consumer<MessageMetadata> metadataSetter;

    public CallBuilder(Bus bus, T request, Class<R> responseType) {
        this.bus = bus;
        this.request = request;
        this.responseType = responseType;
    }

    /**
     * 指定消息所属通道。
     */
    public CallBuilder<T, R> withChannel(String channel) {
        this.channel = channel;
        return this;
    }

    /**
     * 配置管道行为回调。
     */
    public CallBuilder<T, R> withBehavior(Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
        this.behavior = behavior;
        return this;
    }

    /**
     * 指定调用选项。
     */
    public CallBuilder<T, R> withOptions(CallOptions options) {
        this.options = options;
        return this;
    }

    /**
     * 配置元数据设置器。
     */
    public CallBuilder<T, R> withMetadata(Consumer<MessageMetadata> metadataSetter) {
        this.metadataSetter = metadataSetter;
        return this;
    }

    /**
     * 执行请求。
     *
     * @return 在收到响应时完成并携带响应结果的 future
     */
    public CompletableFuture<R> execute() {
        var opts = options != null ? options : new CallOptions();
        if (channel != null) {
            opts.setChannel(channel);
        }
        return bus.callAsync(request, responseType, behavior, opts, metadataSetter);
    }
}
