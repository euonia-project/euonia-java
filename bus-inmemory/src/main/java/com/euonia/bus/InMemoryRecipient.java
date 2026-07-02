package com.euonia.bus;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.dlq.DeadLetterMessage;
import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.messenger.Recipient;

/**
 * 内存消息接收者的抽象基类。
 * <p>
 * 此类实现 {@link Recipient}{@code <}{@link MessagePack}{@code >} 并提供
 * 结构化的消息处理管道：当收到 {@link MessagePack} 时，触发
 * {@code messageReceived} 监听器，调用抽象的 {@link HandlerContext#handleAsync} 方法，
 * 然后触发 {@code messageAcknowledged} 监听器。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class InMemoryRecipient implements Recipient<MessagePack> {

    private static final Logger LOGGER = Logger.getLogger(InMemoryRecipient.class.getName());

    private final HandlerContext handler;

    /**
     * 收到消息时（处理前）通知的监听器列表。
     */
    private final CopyOnWriteArrayList<Consumer<MessageReceivedEvent>> messageReceivedListeners = new CopyOnWriteArrayList<>();

    /**
     * 消息确认后（处理完成后）通知的监听器列表。
     */
    private final CopyOnWriteArrayList<Consumer<MessageAcknowledgedEvent>> messageAcknowledgedListeners = new CopyOnWriteArrayList<>();

    /**
     * 死信队列选项，由 {@link InMemoryRecipientRegistrar} 在创建后注入。
     */
    private InMemoryBusOptions deadLetterOptions;

    /**
     * 当前正在处理的路由消息，由 {@link #receive(MessagePack)} 设置，
     * 供子类在 {@link HandlerContext#handleAsync} 的错误路径中调用 {@link #publishDeadLetter} 时读取。
     */
    protected volatile MessageEnvelope<?> currentMessage;

    protected InMemoryRecipient(HandlerContext handler) {
        this.handler = handler;
    }

    /**
     * 设置死信队列选项。由 {@link InMemoryRecipientRegistrar} 在创建接收者后调用。
     *
     * @param options 包含 DLQ 配置的总线选项
     */
    void setDeadLetterOptions(InMemoryBusOptions options) {
        this.deadLetterOptions = options;
    }

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
        MessageEnvelope<?> message = pack.getMessage();
        this.currentMessage = message;
        MessageContext context = pack.getContext();

        onMessageReceived(new MessageReceivedEvent(message, context));

        try {
            handler.handleAsync(message.getChannel(), getName(), message, context)
                   .whenComplete((result, error) -> {
                       if (error != null) {
                           publishDeadLetter(message.getChannel(), message, error);
                       }
                   });
            onMessageAcknowledged(new MessageAcknowledgedEvent(message, context));
        } catch (Exception exception) {
            throw new RuntimeException(String.format("Message '%s' on channel '%s' error: %s", message.getMessageId(), message.getChannel(), exception.getMessage()), exception);
        }
    }

    /**
     * 如果启用了死信队列，则将处理失败的消息发布到 {@link InMemoryDeadLetterQueue}。
     *
     * @param channel 消息通道
     * @param message 原始消息负载
     * @param error   处理过程中抛出的异常
     */
    protected void publishDeadLetter(String channel, MessageEnvelope<?> message, Throwable error) {
        if (deadLetterOptions != null && deadLetterOptions.isDeadLetterEnabled()) {
            InMemoryDeadLetterQueue.getInstance().publish(channel, new DeadLetterMessage<>(message, error));
        }
    }

    public abstract String getName();
}
