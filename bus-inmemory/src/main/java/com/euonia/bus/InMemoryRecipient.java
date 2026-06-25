package com.euonia.bus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.messenger.Recipient;

/**
 * 内存消息接收者的抽象基类。
 * <p>
 * 此类实现 {@link Recipient}{@code <}{@link MessagePack}{@code >} 并提供
 * 结构化的消息处理管道：当收到 {@link MessagePack} 时，触发
 * {@code messageReceived} 监听器，调用抽象的 {@link #handleAsync} 方法，
 * 然后触发 {@code messageAcknowledged} 监听器。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class InMemoryRecipient implements Recipient<MessagePack> {

    private static final Logger LOGGER = Logger.getLogger(InMemoryRecipient.class.getName());

    /**
     * 收到消息时（处理前）通知的监听器列表。
     */
    private final CopyOnWriteArrayList<Consumer<MessageReceivedEvent>> messageReceivedListeners = new CopyOnWriteArrayList<>();

    /**
     * 消息确认后（处理完成后）通知的监听器列表。
     */
    private final CopyOnWriteArrayList<Consumer<MessageAcknowledgedEvent>> messageAcknowledgedListeners = new CopyOnWriteArrayList<>();

    /**
     * 添加消息接收事件的监听器。
     *
     * @param listener 要添加的监听器
     */
    public void addMessageReceivedListener(Consumer<MessageReceivedEvent> listener) {
        messageReceivedListeners.add(listener);
    }

    /**
     * 移除消息接收事件的监听器。
     *
     * @param listener 要移除的监听器
     */
    public void removeMessageReceivedListener(Consumer<MessageReceivedEvent> listener) {
        messageReceivedListeners.remove(listener);
    }

    /**
     * 添加消息确认事件的监听器。
     *
     * @param listener 要添加的监听器
     */
    public void addMessageAcknowledgedListener(Consumer<MessageAcknowledgedEvent> listener) {
        messageAcknowledgedListeners.add(listener);
    }

    /**
     * 移除消息确认事件的监听器。
     *
     * @param listener 要移除的监听器
     */
    public void removeMessageAcknowledgedListener(Consumer<MessageAcknowledgedEvent> listener) {
        messageAcknowledgedListeners.remove(listener);
    }

    /**
     * 触发 {@code messageReceived} 事件。
     *
     * @param event 事件对象
     */
    protected void onMessageReceived(MessageReceivedEvent event) {
        for (Consumer<MessageReceivedEvent> listener : messageReceivedListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in message received listener: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 触发 {@code messageAcknowledged} 事件。
     *
     * @param event 事件对象
     */
    protected void onMessageAcknowledged(MessageAcknowledgedEvent event) {
        for (Consumer<MessageAcknowledgedEvent> listener : messageAcknowledgedListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in message acknowledged listener: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 接收消息包并通过管道进行处理。
     *
     * @param pack 要处理的消息包
     */
    @Override
    public void receive(MessagePack pack) {
        RoutedMessage<?> message = pack.getMessage();
        MessageContext context = pack.getContext();

        onMessageReceived(new MessageReceivedEvent(message, context));

        try {
            handleAsync(message.getChannel(), message.getPayload(), context, pack.isAborted());
            onMessageAcknowledged(new MessageAcknowledgedEvent(message, context));
        } catch (Exception exception) {
            throw new RuntimeException(String.format("Message '%s' on channel '%s' error: %s", message.getMessageId(), message.getChannel(), exception.getMessage()), exception);
        }
    }

    /**
     * 异步处理收到的消息。
     * <p>
     * 实现应返回一个 {@link CompletableFuture}，在消息完全处理完成后完成。
     *
     * @param channel 消息发送的通道（主题/队列）
     * @param message 消息负载
     * @param context 消息处理上下文
     * @param aborted 消息是否已被标记为中止
     * @return 处理完成后完成的 future
     */
    protected abstract CompletableFuture<Object> handleAsync(String channel, Object message, MessageContext context, boolean aborted);
}
