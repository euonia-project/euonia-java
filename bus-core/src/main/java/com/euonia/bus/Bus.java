package com.euonia.bus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import com.euonia.bus.contract.Request;
import com.euonia.bus.message.PipelineMessage;
import com.euonia.bus.options.CallOptions;
import com.euonia.bus.options.PublishOptions;
import com.euonia.bus.options.SendOptions;

/**
 * 消息总线接口，定义了消息的发布、发送和请求-响应调用的核心契约。
 * <p>
 * 提供三种消息传递模式的抽象方法和一系列便利默认方法：
 * <ul>
 *   <li><b>发布/订阅（Publish）</b> — 通过 {@link #publishAsync} 系列方法将多播消息发送到所有订阅者</li>
 *   <li><b>发送/命令（Send）</b> — 通过 {@link #sendAsync} 系列方法将单播消息发送到单个处理程序</li>
 *   <li><b>请求/响应（Call）</b> — 通过 {@link #callAsync} 系列方法发送请求并期待类型化的响应</li>
 * </ul>
 * <p>
 * 每个系列的默认方法提供了渐进式的参数组合，最终委托到对应的抽象方法。
 *
 * @author damon(zhaorong@outlook)
 */
public interface Bus {
    default <T> CompletableFuture<Void> publishAsync(T message) {
        return publishAsync(message, null);
    }

    default <T> CompletableFuture<Void> publishAsync(T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
        return publishAsync(message, behavior, new PublishOptions());
    }

    default <T> CompletableFuture<Void> publishAsync(T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior, PublishOptions options) {
        return publishAsync(message, behavior, options, null);
    }

    /**
     * 以发布/订阅模式异步发布一条多播消息（完整参数版本）。
     *
     * @param <T>            消息负载类型
     * @param message        要发布的消息
     * @param behavior       可选的管道行为配置回调，可以为 {@code null}
     * @param options        发布选项
     * @param metadataSetter 可选的元数据设置器，可以为 {@code null}
     * @return 在所有传输实例完成发送后完成的 future
     */
    <T> CompletableFuture<Void> publishAsync(T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior, PublishOptions options, Consumer<MessageMetadata> metadataSetter);

    default <T> CompletableFuture<Void> publishAsync(String channel, T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior, Consumer<MessageMetadata> metadataSetter) {
        var options = new PublishOptions();
        options.setChannel(channel);
        return publishAsync(message, behavior, options, metadataSetter);
    }

    default <T> CompletableFuture<Void> sendAsync(T message) {
        return sendAsync(message, null);
    }

    default <T> CompletableFuture<Void> sendAsync(T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior) {
        return sendAsync(message, behavior, new SendOptions());
    }

    default <T> CompletableFuture<Void> sendAsync(T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior, SendOptions sendOptions) {
        return sendAsync(message, behavior, sendOptions, null);
    }

    default <T> CompletableFuture<Void> sendAsync(T message, Consumer<PipelineMessage<RoutedMessage<T>, Void>> behavior, SendOptions sendOptions, Consumer<MessageMetadata> metadataSetter) {
        return sendAsync(message, Void.class, null, behavior, sendOptions, metadataSetter);
    }

    default <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback) {
        return sendAsync(message, responseType, callback, null);
    }

    default <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
        return sendAsync(message, responseType, callback, behavior, new SendOptions());
    }

    default <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior, SendOptions sendOptions) {
        return sendAsync(message, responseType, callback, behavior, sendOptions, null);
    }

    /**
     * 以发送/命令模式异步发送一条单播消息并等待响应（完整参数版本）。
     *
     * @param <T>            消息负载类型
     * @param <R>            响应类型
     * @param message        要发送的消息
     * @param responseType   期望的响应类型
     * @param callback       可选的回调，用于接收响应或错误
     * @param behavior       可选的管道行为配置回调，可以为 {@code null}
     * @param sendOptions    发送选项
     * @param metadataSetter 可选的元数据设置器，可以为 {@code null}
     * @return 在消息处理完毕时完成的 future
     */
    <T, R> CompletableFuture<Void> sendAsync(T message, Class<R> responseType, Flow.Subscriber<R> callback, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior, SendOptions sendOptions, Consumer<MessageMetadata> metadataSetter);

    default <T extends Request<R>, R> CompletableFuture<R> callAsync(T request, Class<R> responseType) {
        return callAsync(request, responseType, null);
    }

    default <T extends Request<R>, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior) {
        return callAsync(request, responseType, behavior, new CallOptions());
    }

    default <T extends Request<R>, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior, CallOptions callOptions) {
        return callAsync(request, responseType, behavior, callOptions, null);
    }

    /**
     * 以请求/响应模式异步发送一条请求消息并期待类型化的响应（完整参数版本）。
     *
     * @param <T>            请求类型，必须实现 {@link Request}
     * @param <R>            响应类型
     * @param request        请求消息
     * @param responseType   期望的响应类型
     * @param behavior       可选的管道行为配置回调，可以为 {@code null}
     * @param callOptions    调用选项
     * @param metadataSetter 可选的元数据设置器，可以为 {@code null}
     * @return 在收到响应时完成并携带响应结果的 future
     */
    <T extends Request<R>, R> CompletableFuture<R> callAsync(T request, Class<R> responseType, Consumer<PipelineMessage<RoutedMessage<T>, R>> behavior, CallOptions callOptions, Consumer<MessageMetadata> metadataSetter);
}
