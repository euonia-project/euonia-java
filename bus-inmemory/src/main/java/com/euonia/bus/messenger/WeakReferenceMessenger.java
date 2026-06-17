package com.euonia.bus.messenger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Messenger} 的弱引用实现。
 * <p>
 * 此 Messenger 使用 {@link WeakReference} 来跟踪已注册的接收者，因此不再被其他地方引用的接收者将被自动垃圾回收。
 * 如果接收者未手动取消注册，这样可以避免内存泄漏。
 *
 * <p><b>线程安全：</b>所有公开方法都是线程安全的。
 *
 * @author damon(zhaorong@outlook)
 * @see StrongReferenceMessenger
 */
public final class WeakReferenceMessenger implements Messenger {

    // ---- 默认单例 ----

    private static final WeakReferenceMessenger DEFAULT = new WeakReferenceMessenger();

    public static WeakReferenceMessenger getDefault() {
        return DEFAULT;
    }

    // ---- 内部数据结构 ----

    /**
     * 所有已注册的处理器：消息类 → 通道 → （弱引用接收者 → 分发器）。
     * 使用 {@link WeakKey} 作为键，以便接收者可以被垃圾回收。
     */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<WeakKey, MessageHandlerDispatcher>>> handlers
            = new ConcurrentHashMap<>();

    /**
     * 跟踪每个接收者的弱引用，用于清理目的。
     * 将接收者身份哈希映射到 (消息类, 通道) 集合，以便批量取消注册。
     */
    private final ConcurrentHashMap<WeakKey, Set<MessageChannelKey>> recipientKeys
            = new ConcurrentHashMap<>();

    // ---- 是否已注册 ----

    @Override
    public <TMessage> boolean isRegistered(Object recipient, Class<TMessage> messageType) {
        return isRegistered(recipient, messageType, DEFAULT_CHANNEL);
    }

    @Override
    public <TMessage> boolean isRegistered(Object recipient, Class<TMessage> messageType, String channel) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(channel, "channel must not be null");

        WeakKey key = new WeakKey(recipient);
        ConcurrentHashMap<String, ConcurrentHashMap<WeakKey, MessageHandlerDispatcher>> channelMap =
                handlers.get(messageType);
        if (channelMap == null) {
            return false;
        }
        ConcurrentHashMap<WeakKey, MessageHandlerDispatcher> recipientMap = channelMap.get(channel);
        if (recipientMap == null) {
            return false;
        }
        return recipientMap.containsKey(key);
    }

    // ---- 注册 ----

    @Override
    public <TRecipient, TMessage> void register(
            TRecipient recipient,
            Class<TMessage> messageType,
            MessageHandler<TRecipient, TMessage> handler) {
        register(recipient, messageType, DEFAULT_CHANNEL, handler);
    }

    @Override
    public <TRecipient, TMessage> void register(
            TRecipient recipient,
            Class<TMessage> messageType,
            String channel,
            MessageHandler<TRecipient, TMessage> handler) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(handler, "handler must not be null");

        registerInternal(recipient, messageType, channel,
                new MessageHandlerDispatcher.For<>(handler));
    }

    @Override
    public <TMessage> void register(Recipient<TMessage> recipient, Class<TMessage> messageType) {
        register(recipient, messageType, DEFAULT_CHANNEL);
    }

    @Override
    public <TMessage> void register(Recipient<TMessage> recipient, Class<TMessage> messageType, String channel) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(channel, "channel must not be null");

        // 使用 NULL 标记来指示 IRecipient 快速路径
        registerInternal(recipient, messageType, channel, MessageHandlerDispatcher.NULL);
    }

    private void registerInternal(
            Object recipient,
            Class<?> messageType,
            String channel,
            MessageHandlerDispatcher dispatcher) {

        WeakKey key = new WeakKey(recipient);

        // 获取或创建此消息类型的通道映射
        ConcurrentHashMap<String, ConcurrentHashMap<WeakKey, MessageHandlerDispatcher>> channelMap =
                handlers.computeIfAbsent(messageType, k -> new ConcurrentHashMap<>());

        // 获取或创建此通道的接收者映射
        ConcurrentHashMap<WeakKey, MessageHandlerDispatcher> recipientMap =
                channelMap.computeIfAbsent(channel, k -> new ConcurrentHashMap<>());

        // 注册处理器，重复时抛出异常
        MessageHandlerDispatcher existing = recipientMap.putIfAbsent(key, dispatcher);
        if (existing != null) {
            throw new IllegalStateException(
                    "The target recipient has already subscribed to the target message: "
                    + "messageType=" + messageType.getSimpleName()
                    + ", channel=" + channel);
        }

        // 跟踪此接收者的注册
        MessageChannelKey mck = new MessageChannelKey(messageType, channel);
        recipientKeys.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(mck);
    }

    // ---- 发送 ----

    @Override
    public <TMessage> TMessage send(TMessage message) {
        return send(message, DEFAULT_CHANNEL);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TMessage> TMessage send(TMessage message, String channel) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(channel, "channel must not be null");

        Class<?> messageType = message.getClass();
        ConcurrentHashMap<String, ConcurrentHashMap<WeakKey, MessageHandlerDispatcher>> channelMap =
                handlers.get(messageType);

        if (channelMap == null) {
            return message;
        }

        ConcurrentHashMap<WeakKey, MessageHandlerDispatcher> recipientMap = channelMap.get(channel);
        if (recipientMap == null || recipientMap.isEmpty()) {
            return message;
        }

        // 创建快照以避免 ConcurrentModificationException
        List<Map.Entry<WeakKey, MessageHandlerDispatcher>> snapshot = new ArrayList<>(recipientMap.entrySet());

        for (Map.Entry<WeakKey, MessageHandlerDispatcher> entry : snapshot) {
            MessageHandlerDispatcher dispatcher = entry.getValue();
            Object recipient = entry.getKey().get();

            if (recipient == null) {
                // 目标已被垃圾回收——跳过
                continue;
            }

            if (dispatcher == MessageHandlerDispatcher.NULL) {
                // 快速路径：接收者实现了 IRecipient<TMessage>
                ((Recipient<TMessage>) recipient).receive(message);
            } else {
                dispatcher.invoke(recipient, message);
            }
        }

        return message;
    }

    // ---- 取消注册 ----

    @Override
    public void unregisterAll(Object recipient) {
        Objects.requireNonNull(recipient, "recipient must not be null");

        WeakKey key = new WeakKey(recipient);
        Set<MessageChannelKey> keys = recipientKeys.remove(key);
        if (keys == null) {
            return;
        }

        for (MessageChannelKey mck : keys) {
            removeRegistration(mck, key);
        }
    }

    @Override
    public void unregisterAll(Object recipient, String channel) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(channel, "channel must not be null");

        if (DEFAULT_CHANNEL.equals(channel)) {
            throw new UnsupportedOperationException(
                    "Use unregisterAll(Object) to unregister from the default channel");
        }

        WeakKey key = new WeakKey(recipient);
        Set<MessageChannelKey> keys = recipientKeys.get(key);
        if (keys == null) {
            return;
        }

        List<MessageChannelKey> toRemove = new ArrayList<>();
        for (MessageChannelKey mck : keys) {
            if (mck.channel().equals(channel)) {
                toRemove.add(mck);
            }
        }

        for (MessageChannelKey mck : toRemove) {
            removeRegistration(mck, key);
            keys.remove(mck);
        }

        if (keys.isEmpty()) {
            recipientKeys.remove(key);
        }
    }

    @Override
    public <TMessage> void unregister(Object recipient, Class<TMessage> messageType) {
        unregister(recipient, messageType, DEFAULT_CHANNEL);
    }

    @Override
    public <TMessage> void unregister(Object recipient, Class<TMessage> messageType, String channel) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(channel, "channel must not be null");

        WeakKey key = new WeakKey(recipient);
        MessageChannelKey mck = new MessageChannelKey(messageType, channel);

        removeRegistration(mck, key);

        Set<MessageChannelKey> keys = recipientKeys.get(key);
        if (keys != null) {
            keys.remove(mck);
            if (keys.isEmpty()) {
                recipientKeys.remove(key);
            }
        }
    }

    private void removeRegistration(MessageChannelKey mck, WeakKey key) {
        ConcurrentHashMap<String, ConcurrentHashMap<WeakKey, MessageHandlerDispatcher>> channelMap =
                handlers.get(mck.messageType());
        if (channelMap != null) {
            ConcurrentHashMap<WeakKey, MessageHandlerDispatcher> recipientMap =
                    channelMap.get(mck.channel());
            if (recipientMap != null) {
                recipientMap.remove(key);
                if (recipientMap.isEmpty()) {
                    channelMap.remove(mck.channel(), recipientMap);
                }
            }
            if (channelMap.isEmpty()) {
                handlers.remove(mck.messageType(), channelMap);
            }
        }
    }

    // ---- 生命周期 ----

    @Override
    public void cleanup() {
        // 移除其接收者已被垃圾回收的条目
        List<WeakKey> collectedKeys = new ArrayList<>();

        for (Map.Entry<WeakKey, Set<MessageChannelKey>> entry : recipientKeys.entrySet()) {
            if (entry.getKey().get() == null) {
                collectedKeys.add(entry.getKey());
            }
        }

        for (WeakKey key : collectedKeys) {
            Set<MessageChannelKey> keys = recipientKeys.remove(key);
            if (keys != null) {
                for (MessageChannelKey mck : keys) {
                    removeRegistration(mck, key);
                }
            }
        }
    }

    @Override
    public void reset() {
        handlers.clear();
        recipientKeys.clear();
    }

    // ---- 内部类型 ----

    /**
     * 标识特定 (消息类型, 通道) 注册对的复合键。
     */
    private record MessageChannelKey(Class<?> messageType, String channel) {}

    /**
     * 基于 {@link WeakReference} 的映射键，用于包装接收者。
     * 相等性和哈希码由被引用对象（被包装的对象）计算，
     * 因此相同的接收者对象映射到相同的键。
     */
    private static final class WeakKey extends WeakReference<Object> {
        private final int hashCode;

        WeakKey(Object referent) {
            super(Objects.requireNonNull(referent, "referent must not be null"));
            this.hashCode = System.identityHashCode(referent);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof WeakKey other)) return false;
            Object a = this.get();
            Object b = other.get();
            // 如果任一已被回收，则回退到引用相等性
            if (a == null || b == null) return this == other;
            return a == b;  // 身份相等性
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
