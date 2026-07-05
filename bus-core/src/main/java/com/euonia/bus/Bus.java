package com.euonia.bus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import com.euonia.bus.builder.CallBuilder;
import com.euonia.bus.builder.PublishBuilder;
import com.euonia.bus.builder.SendBuilder;
import com.euonia.bus.message.Request;
import com.euonia.bus.options.CallOptions;
import com.euonia.bus.options.PublishOptions;
import com.euonia.bus.options.SendOptions;

/**
 * 消息总线接口，定义了消息的发布、发送和请求-响应调用的核心契约。
 * <p>
 * 提供三种消息传递模式：
 * <ul>
 *   <li><b>发布/订阅（Publish）</b> — 通过 {@link #publish} 构建器将多播消息发送到所有订阅者</li>
 *   <li><b>发送/命令（Send）</b> — 通过 {@link #send} 构建器将单播消息发送到单个处理程序</li>
 *   <li><b>请求/响应（Call）</b> — 通过 {@link #call} 构建器发送请求并期待类型化的响应</li>
 * </ul>
 *
 * <h3>Builder 模式（推荐）</h3>
 * <pre>{@code
 * // 发布
 * bus.publish(event).withChannel("orders").execute();
 *
 * // 发送（不关心响应）
 * bus.send(command).withChannel("commands").execute();
 *
 * // 发送（带回调）
 * bus.send(command, Result.class)
 *    .withCallback(subscriber)
 *    .execute();
 *
 * // 请求-响应
 * Result result = bus.call(request, Result.class)
 *                    .withChannel("queries")
 *                    .execute()
 *                    .get();
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Bus {

    /**
     * 创建发布/订阅模式的 Builder。
     *
     * @param <T>     消息负载类型
     * @param message 要发布的消息
     * @return {@link PublishBuilder} 实例
     */
    default <T> PublishBuilder<T> publish(T message) {
        return new PublishBuilder<>(this, message);
    }

    /**
     * 创建发送/命令模式的 Builder（不关心响应类型）。
     *
     * @param <T>     消息负载类型
     * @param message 要发送的消息
     * @return {@link SendBuilder} 实例
     */
    default <T> SendBuilder<T, Void> send(T message) {
        return new SendBuilder<>(this, message, Void.class);
    }

    /**
     * 创建发送/命令模式的 Builder（指定响应类型）。
     *
     * @param <T>          消息负载类型
     * @param <R>          响应类型
     * @param message      要发送的消息
     * @param responseType 期望的响应类型
     * @return {@link SendBuilder} 实例
     */
    default <T, R> SendBuilder<T, R> send(T message, Class<R> responseType) {
        return new SendBuilder<>(this, message, responseType);
    }

    /**
     * 创建请求/响应模式的 Builder。
     *
     * @param <T>          请求类型，必须实现 {@link Request}
     * @param <R>          响应类型
     * @param request      请求消息
     * @param responseType 期望的响应类型
     * @return {@link CallBuilder} 实例
     */
    default <T, R> CallBuilder<T, R> call(T request, Class<R> responseType) {
        return new CallBuilder<>(this, request, responseType);
    }

    /**
     * 以发布/订阅模式异步发布一条多播消息（完整参数版本）。
     *
     * @param <T>      消息负载类型
     * @param message  要发布的消息
     * @param options  发布选项
     * @param behavior 可选的管道行为配置回调，可以为 {@code null}
     * @return 在所有传输实例完成发送后完成的 future
     */
    <T> CompletableFuture<Void> publishAsync(T message, PublishOptions options, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior);

    /**
     * 以发送/命令模式异步发送一条单播消息并等待响应（完整参数版本）。
     *
     * @param <T>          消息负载类型
     * @param <R>          响应类型
     * @param message      要发送的消息
     * @param responseType 期望的响应类型
     * @param callback     可选的回调，用于接收响应或错误
     * @param sendOptions  发送选项
     * @param behavior     可选的管道行为配置回调，可以为 {@code null}
     * @return 在消息处理完毕时完成的 future
     */
    <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback, SendOptions sendOptions, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior);

    /**
     * 以请求/响应模式异步发送一条请求消息并期待类型化的响应（完整参数版本）。
     *
     * @param <T>          请求类型，必须实现 {@link Request}
     * @param <R>          响应类型
     * @param request      请求消息
     * @param responseType 期望的响应类型
     * @param callOptions  调用选项
     * @param behavior     可选的管道行为配置回调，可以为 {@code null}
     * @return 在收到响应时完成并携带响应结果的 future
     */
    <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, CallOptions callOptions, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior);

    /**
     * 以发布/订阅模式异步发布一条多播消息。
     *
     * @param message 要发布的消息
     * @param <T>     消息负载类型
     * @return 在所有传输实例完成发送后完成的 future
     */
    default <T> CompletableFuture<Void> publishAsync(T message) {
        return publishAsync(message, new PublishOptions(), null);
    }

    /**
     * 以发送/命令模式异步发送一条单播消息并等待响应。
     *
     * @param message 要发送的消息
     * @param <T>     消息负载类型
     * @return 在消息处理完毕时完成的 future
     */
    default <T> CompletableFuture<Void> sendAsync(T message) {
        return sendAsync(message, Void.class, null, new SendOptions(), null);
    }

    /**
     * 以发送/命令模式异步发送一条单播消息并等待响应。
     *
     * @param message      要发送的消息
     * @param responseType 期望的响应类型
     * @param callback     可选的回调，用于接收响应或错误
     * @param <T>          消息负载类型
     * @param <R>          响应类型
     * @return 在消息处理完毕时完成的 future
     */
    default <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback) {
        return sendAsync(message, responseType, callback, new SendOptions(), null);
    }

    /**
     * 以请求/响应模式异步发送一条请求消息并期待类型化的响应。
     *
     * @param request      请求消息
     * @param responseType 期望的响应类型
     * @param <T>          请求类型，必须实现 {@link Request}
     * @param <R>          响应类型
     * @return 在收到响应时完成并携带响应结果的 future
     */
    default <T, R> CompletableFuture<R> callAsync(T request, Class<R> responseType) {
        return callAsync(request, responseType, new CallOptions(), null);
    }
}
