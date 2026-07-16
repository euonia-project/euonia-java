package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import com.euonia.bus.Bus;
import com.euonia.bus.options.SendOptions;
import com.euonia.core.ArgumentNullException;

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
public final class SendBuilder<T, R> extends AbstractBuilder<SendBuilder<T, R>, SendOptions, T, R> {

    private final Bus bus;
    private final T message;
    private final Class<R> responseType;
    private Flow.Subscriber<R> callback;

    public SendBuilder(Bus bus, T message, Class<R> responseType) {
        super(new SendOptions());
        this.bus = bus;
        this.message = message;
        this.responseType = responseType;
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
     * 执行发送。
     *
     * @return 在消息处理完毕时完成的 future
     */
    public CompletableFuture<Void> executeAsync() {
        return bus.sendAsync(message, responseType, callback, options, behavior);
    }
}
