package com.euonia.bus.messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Messenger} 的强引用实现。
 * <p>
 * 此 Messenger 使用强引用来跟踪已注册的接收者。当不再需要接收者时，<b>必须</b>手动取消注册以避免内存泄漏。
 *
 * <p>
 * <b>线程安全：</b>所有公开方法都是线程安全的。
 *
 * @author damon(zhaorong@outlook.com)
 * @see WeakReferenceMessenger
 */
public final class StrongReferenceMessenger implements Messenger {

    // ---- 默认单例 ----

    private static final StrongReferenceMessenger DEFAULT = new StrongReferenceMessenger();

    public static StrongReferenceMessenger getDefault() {
        return DEFAULT;
    }

    // ---- 内部数据结构 ----

    /**
     * 所有已注册的处理器：消息类 → 通道 → （接收者 → 分发器）。
     */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>>> handlers = new ConcurrentHashMap<>();

    /**
     * 跟踪每个接收者注册了哪些 (消息类, 通道) 对，
     * 以便 unregisterAll 可以高效扫描。
     */
    private final ConcurrentHashMap<RecipientKey, Set<MessageChannelKey>> recipientKeys = new ConcurrentHashMap<>();

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

        RecipientKey key = new RecipientKey(recipient);
        ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>> channelMap = handlers
                .get(messageType);
        if (channelMap == null) {
            return false;
        }
        ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher> recipientMap = channelMap.get(channel);
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

        RecipientKey key = new RecipientKey(recipient);

        // 获取或创建此消息类型的通道映射
        ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>> channelMap = handlers
                .computeIfAbsent(messageType, k -> new ConcurrentHashMap<>());

        // 获取或创建此通道的接收者映射
        ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher> recipientMap = channelMap.computeIfAbsent(channel,
                k -> new ConcurrentHashMap<>());

        // 注册处理器，重复时抛出异常
        MessageHandlerDispatcher existing = recipientMap.putIfAbsent(key, dispatcher);
        if (existing != null) {
            throw new IllegalStateException(
                    "The target recipient has already subscribed to the target message: "
                            + "messageType=" + messageType.getSimpleName()
                            + ", channel=" + channel
                            + ", recipient=" + recipient.getClass().getSimpleName());
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
        ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>> channelMap = handlers
                .get(messageType);

        if (channelMap == null) {
            return message;
        }

        ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher> recipientMap = channelMap.get(channel);
        if (recipientMap == null || recipientMap.isEmpty()) {
            return message;
        }

        // 创建当前处理器的快照，以避免广播期间处理器注册/取消注册导致的问题
        List<Map.Entry<RecipientKey, MessageHandlerDispatcher>> snapshot = new ArrayList<>(recipientMap.entrySet());

        for (Map.Entry<RecipientKey, MessageHandlerDispatcher> entry : snapshot) {
            MessageHandlerDispatcher dispatcher = entry.getValue();
            Object recipient = entry.getKey().getTarget();

            if (recipient == null) {
                // 目标已被垃圾回收（强引用不应发生此情况，但安全起见）
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

        RecipientKey key = new RecipientKey(recipient);
        Set<MessageChannelKey> keys = recipientKeys.remove(key);
        if (keys == null) {
            return;
        }

        for (MessageChannelKey mck : keys) {
            ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>> channelMap = handlers
                    .get(mck.messageType());
            if (channelMap != null) {
                ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher> recipientMap = channelMap.get(mck.channel());
                if (recipientMap != null) {
                    recipientMap.remove(key);
                    // Clean up empty maps
                    if (recipientMap.isEmpty()) {
                        channelMap.remove(mck.channel(), recipientMap);
                    }
                }
                if (channelMap.isEmpty()) {
                    handlers.remove(mck.messageType(), channelMap);
                }
            }
        }
    }

    @Override
    public void unregisterAll(Object recipient, String channel) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(channel, "channel must not be null");

        if (DEFAULT_CHANNEL.equals(channel)) {
            // 对于默认通道，unregisterAll(Object) 已经涵盖了它
            throw new UnsupportedOperationException(
                    "Use unregisterAll(Object) to unregister from the default channel");
        }

        RecipientKey key = new RecipientKey(recipient);
        Set<MessageChannelKey> keys = recipientKeys.get(key);
        if (keys == null) {
            return;
        }

        // 收集要移除的匹配键
        List<MessageChannelKey> toRemove = new ArrayList<>();
        for (MessageChannelKey mck : keys) {
            if (mck.channel().equals(channel)) {
                toRemove.add(mck);
            }
        }

        for (MessageChannelKey mck : toRemove) {
            ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>> channelMap = handlers
                    .get(mck.messageType());
            if (channelMap != null) {
                ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher> recipientMap = channelMap.get(mck.channel());
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
            keys.remove(mck);
        }

        // If the recipient has no more registrations, remove it
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

        RecipientKey key = new RecipientKey(recipient);
        MessageChannelKey mck = new MessageChannelKey(messageType, channel);

        // 从处理器中移除
        ConcurrentHashMap<String, ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher>> channelMap = handlers
                .get(messageType);
        if (channelMap != null) {
            ConcurrentHashMap<RecipientKey, MessageHandlerDispatcher> recipientMap = channelMap.get(channel);
            if (recipientMap != null) {
                recipientMap.remove(key);
                if (recipientMap.isEmpty()) {
                    channelMap.remove(channel, recipientMap);
                }
            }
            if (channelMap.isEmpty()) {
                handlers.remove(messageType, channelMap);
            }
        }

        // 从接收者跟踪中移除
        Set<MessageChannelKey> keys = recipientKeys.get(key);
        if (keys != null) {
            keys.remove(mck);
            if (keys.isEmpty()) {
                recipientKeys.remove(key);
            }
        }
    }

    // ---- 生命周期 ----

    @Override
    public void cleanup() {
        // 强引用不需要清理——所有注册都是显式的。
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
    private record MessageChannelKey(Class<?> messageType, String channel) {
    }

    /**
     * 包装接收者对象以用作映射键。使用基于身份相等性的比较，
     * 使得即使两个不同的对象 .equals() 为 true，
     * 它们仍会被视为不同的接收者。
     */
    private static final class RecipientKey {
        private final Object target;

        RecipientKey(Object target) {
            this.target = Objects.requireNonNull(target, "target must not be null");
        }

        Object getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof RecipientKey other))
                return false;
            return target == other.target; // 身份相等性
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(target);
        }

        @Override
        public String toString() {
            return "RecipientKey{" + target.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "}";
        }
    }
}
