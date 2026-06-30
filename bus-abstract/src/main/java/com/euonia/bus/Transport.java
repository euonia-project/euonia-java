package com.euonia.bus;

import java.util.concurrent.CompletableFuture;

/**
 * {@link Transport} 负责将消息发送到系统中的其他节点。
 * 提供发布事件、发送命令和进行请求-响应调用的方法。
 * <p>
 * 每种传输实现可以使用不同的底层通信协议（如 HTTP、gRPC、WebSocket 等）
 * 来发送消息。传输还负责处理消息的序列化和反序列化，以及管理连接和确保消息传递。
 * <p>
 * {@link Transport} 接口定义了发送消息的契约，可以创建不同的实现来支持
 * 各种通信协议和模式。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Transport {
    /**
     * 获取传输名称。可用于标识当前使用的传输实现（如 "HTTP"、"gRPC"、"WebSocket" 等）。
     *
     * @return 传输名称
     */
    String getName();

    /**
     * 向传输发布消息。
     * 此方法通常用于发送不期望响应的事件或消息。
     * 消息以异步方式发送，返回一个 {@link CompletableFuture}，
     * 该 Future 在消息成功发布后完成。
     *
     * @param <M>     消息类型
     * @param message 要发布的消息
     * @return 在消息成功发布后完成的 {@link CompletableFuture}
     */
    <M> CompletableFuture<Void> publishAsync(RoutedMessage<M> message);

    /**
     * 向指定目标发送消息。此方法通常用于发送期望响应的命令或消息。
     * 消息以异步方式发送，返回一个 {@link CompletableFuture}，
     * 该 Future 在消息成功发送后完成。
     *
     * @param <M>     消息类型
     * @param message 要发送的消息
     * @return 在消息成功发送后完成的 {@link CompletableFuture}
     */
    <M> CompletableFuture<Void> sendAsync(RoutedMessage<M> message);

    /**
     * 发送请求消息并等待响应。此方法通常用于请求-响应通信模式。
     * 消息以异步方式发送，返回一个 {@link CompletableFuture}，
     * 该 Future 在收到响应时完成。
     * {@code responseType} 参数指定了期望的响应消息类型，
     * 允许传输层适当处理反序列化和类型转换。
     *
     * @param <M>          请求消息类型
     * @param <R>          响应消息类型
     * @param message      要发送的请求消息
     * @param responseType 期望的响应消息类型
     * @return 在收到响应时完成的 {@link CompletableFuture}
     */
    <M, R> CompletableFuture<R> sendAsync(RoutedMessage<M> message, Class<R> responseType);

    /**
     * 发送请求消息并等待响应。此方法通常用于 RPC 调用模式。
     * 消息以异步方式发送，返回一个 {@link CompletableFuture}，
     * 该 Future 在收到响应时完成。
     *
     * @param <M>          请求消息类型
     * @param <R>          响应消息类型
     * @param message      要发送的请求消息
     * @param responseType 期望的响应消息类型，允许传输层适当处理反序列化和类型转换
     * @return 在收到响应时完成的 {@link CompletableFuture}
     */
    <M, R> CompletableFuture<R> callAsync(RoutedMessage<M> message, Class<R> responseType);
}
