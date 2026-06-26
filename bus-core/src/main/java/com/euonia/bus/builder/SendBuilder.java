package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import com.euonia.bus.Bus;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.RoutedMessage;
import com.euonia.bus.message.PipelineMessage;
import com.euonia.bus.options.SendOptions;

/**
 * 发送/命令模式的 Builder。
 * <p>
 * 不关心响应时：
 * <pre>{@code
 * bus.send(command)
 *    .withChannel("orders")
 *    .execute();
 * }</pre>
 * <p>
 * 需要异步回调响应时：
 * <pre>{@code
 * bus.send(command, Result.class)
 *    .withCallback(new Flow.Subscriber<>() { ... })
 *    .execute();
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
    private String channel;
    private Flow.Subscriber<R> callback;
    private Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior;
    private SendOptions options;
    private Consumer<MessageMetadata> metadataSetter;

    public SendBuilder(Bus bus, T message, Class<R> responseType) {
        this.bus = bus;
        this.message = message;
        this.responseType = responseType;
    }

    /**
     * 指定消息所属通道。
     */
    public SendBuilder<T, R> withChannel(String channel) {
        this.channel = channel;
        return this;
    }

    /**
     * 配置响应回调（兼容 Flow.Subscriber）。
     */
    public SendBuilder<T, R> withCallback(Flow.Subscriber<R> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 配置管道行为回调。
     */
    public SendBuilder<T, R> withBehavior(Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
        this.behavior = behavior;
        return this;
    }

    /**
     * 指定发送选项。
     */
    public SendBuilder<T, R> withOptions(SendOptions options) {
        this.options = options;
        return this;
    }

    /**
     * 配置元数据设置器。
     */
    public SendBuilder<T, R> withMetadata(Consumer<MessageMetadata> metadataSetter) {
        this.metadataSetter = metadataSetter;
        return this;
    }

    /**
     * 执行发送。
     *
     * @return 在消息处理完毕时完成的 future
     */
    public CompletableFuture<Void> execute() {
        var opts = options != null ? options : new SendOptions();
        if (channel != null) {
            opts.setChannel(channel);
        }
        return bus.sendAsync(message, responseType, callback, behavior, opts, metadataSetter);
    }
}
