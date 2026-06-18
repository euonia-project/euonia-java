package com.euonia.bus.messenger;

/**
 * 定义消息传递器的契约，能够在由令牌标识的各个通道上发送消息以及订阅/取消订阅接收者。
 *
 * @author damon(zhaorong@outlook.com)
 * @see StrongReferenceMessenger
 * @see WeakReferenceMessenger
 */
public interface Messenger {

    /**
     * 默认通道令牌。当不需要指定通道时使用此令牌（空字符串）。
     */
    String DEFAULT_CHANNEL = "";

    // ---- 是否已注册 ----

    /**
     * 检查接收者在默认通道上是否为指定的消息类型注册了处理器。
     *
     * @param <TMessage>  消息类型
     * @param recipient   要检查的接收者
     * @param messageType 消息类型的类
     * @return 如果接收者已为该消息类型注册则返回 {@code true}
     */
    <TMessage> boolean isRegistered(Object recipient, Class<TMessage> messageType);

    /**
     * 检查接收者在指定通道上是否为给定的消息类型注册了处理器。
     *
     * @param <TMessage>  消息类型
     * @param recipient   要检查的接收者
     * @param messageType 消息类型的类
     * @param channel     通道标识
     * @return 如果接收者已在该通道上为该消息类型注册则返回 {@code true}
     */
    <TMessage> boolean isRegistered(Object recipient, Class<TMessage> messageType, String channel);

    // ---- 注册 ----

    /**
     * 在默认通道上为接收者注册特定消息类型的处理器。
     *
     * @param <TRecipient> 接收者类型
     * @param <TMessage>   消息类型
     * @param recipient    接收者实例
     * @param messageType  消息类型的类
     * @param handler      发送消息时调用的处理器
     */
    <TRecipient, TMessage> void register(TRecipient recipient, Class<TMessage> messageType,
            MessageHandler<TRecipient, TMessage> handler);

    /**
     * 在指定通道上为接收者注册特定消息类型的处理器。
     *
     * @param <TRecipient> 接收者类型
     * @param <TMessage>   消息类型
     * @param recipient    接收者实例
     * @param messageType  消息类型的类
     * @param channel      通道标识
     * @param handler      发送消息时调用的处理器
     */
    <TRecipient, TMessage> void register(TRecipient recipient, Class<TMessage> messageType, String channel,
            MessageHandler<TRecipient, TMessage> handler);

    /**
     * 在默认通道上为实现了 {@link Recipient} 的接收者注册消息类型。
     * 这是优化后的快速注册路径。
     *
     * @param <TMessage>  消息类型
     * @param recipient   接收者（必须实现 {@link Recipient}）
     * @param messageType 消息类型的类
     */
    <TMessage> void register(Recipient<TMessage> recipient, Class<TMessage> messageType);

    /**
     * 在指定通道上为实现了 {@link Recipient} 的接收者注册消息类型。
     * 这是优化后的快速注册路径。
     *
     * @param <TMessage>  消息类型
     * @param recipient   接收者（必须实现 {@link Recipient}）
     * @param messageType 消息类型的类
     * @param channel     通道标识
     */
    <TMessage> void register(Recipient<TMessage> recipient, Class<TMessage> messageType, String channel);

    // ---- 发送 ----

    /**
     * 向默认通道上所有已注册的接收者发送消息。
     *
     * @param <TMessage> 消息类型
     * @param message    要发送的消息
     * @return 消息本身（用于流式链式调用）
     */
    <TMessage> TMessage send(TMessage message);

    /**
     * 向指定通道上所有已注册的接收者发送消息。
     *
     * @param <TMessage> 消息类型
     * @param message    要发送的消息
     * @param channel    通道标识
     * @return 消息本身（用于流式链式调用）
     */
    <TMessage> TMessage send(TMessage message, String channel);

    // ---- 取消注册 ----

    /**
     * 在所有通道上取消注册接收者的所有消息类型。
     *
     * @param recipient 要取消注册的接收者
     */
    void unregisterAll(Object recipient);

    /**
     * 在指定通道上取消注册接收者的所有消息类型。
     *
     * @param recipient 要取消注册的接收者
     * @param channel   通道标识
     */
    void unregisterAll(Object recipient, String channel);

    /**
     * 在默认通道上取消注册接收者的特定消息类型。
     *
     * @param <TMessage>  消息类型
     * @param recipient   要取消注册的接收者
     * @param messageType 消息类型的类
     */
    <TMessage> void unregister(Object recipient, Class<TMessage> messageType);

    /**
     * 在指定通道上取消注册接收者的特定消息类型。
     *
     * @param <TMessage>  消息类型
     * @param recipient   要取消注册的接收者
     * @param messageType 消息类型的类
     * @param channel     通道标识
     */
    <TMessage> void unregister(Object recipient, Class<TMessage> messageType, String channel);

    // ---- 生命周期 ----

    /**
     * 对当前消息传递器执行清理。此方法不会取消注册任何接收者，
     * 但可能会修剪内部数据结构（例如，移除 {@link WeakReferenceMessenger}
     * 中已被垃圾回收的弱引用）。
     */
    void cleanup();

    /**
     * 重置消息传递器，取消注册所有接收者。
     */
    void reset();
}
