package com.euonia.bus.builder;

import java.util.concurrent.CompletableFuture;

import com.euonia.bus.Bus;
import com.euonia.bus.message.Request;
import com.euonia.bus.options.CallOptions;
import com.euonia.core.ArgumentNullException;

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
public final class CallBuilder<T, R> extends AbstractBuilder<CallBuilder<T, R>, CallOptions, T, R> {

    private final Bus bus;
    private final T request;
    private final Class<R> responseType;

    /**
     * 创建一个新的 CallBuilder 实例。
     *
     * @param bus          消息总线实例
     * @param request      请求对象
     * @param responseType 响应类型
     */
    public CallBuilder(Bus bus, T request, Class<R> responseType) {
        super(new CallOptions());
        this.bus = bus;
        this.request = request;
        this.responseType = responseType;
    }

    /**
     * 指定消息的关联 ID，此 correlationId 用于在接收到回复消息时判断该回复消息是否与当前请求相关联。
     *
     * @param correlationId 请求的 correlationId
     * @return 当前的 CallBuilder 实例
     */
    public CallBuilder<T, R> withCorrelationId(String correlationId) {
        ArgumentNullException.throwIfNullOrEmpty(correlationId, "correlationId");
        options.setCorrelationId(correlationId);
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
