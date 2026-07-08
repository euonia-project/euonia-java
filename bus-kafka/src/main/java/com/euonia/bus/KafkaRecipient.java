package com.euonia.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.serialization.MessageSerializer;

/**
 * Kafka 消息接收者的抽象基类。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class KafkaRecipient implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(KafkaRecipient.class.getName());

    /** Kafka 总线选项 */
    protected final KafkaBusOptions options;
    /** 处理器上下文 */
    protected final HandlerContext handler;
    /** 消息序列化器 */
    protected final MessageSerializer serializer;
    /** 此接收者处理的消息类型 */
    protected final Class<?> messageType;
    /** 消息接收事件监听器列表 */
    private final List<Consumer<MessageReceivedEvent>> messageReceivedListeners = new ArrayList<>();
    /** 消息确认事件监听器列表 */
    private final List<Consumer<MessageAcknowledgedEvent>> messageAcknowledgedListeners = new ArrayList<>();

    /**
     * 使用必要的依赖构造 Kafka 接收者。
     *
     * @param options     Kafka 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 消息类型
     */
    protected KafkaRecipient(KafkaBusOptions options, HandlerContext handler, MessageSerializer serializer, Class<?> messageType) {
        this.options = options;
        this.handler = handler;
        this.serializer = serializer;
        this.messageType = messageType;
    }

    /**
     * 注册消息接收事件监听器。
     *
     * @param listener 消息接收事件消费者
     */
    public void onMessageReceived(Consumer<MessageReceivedEvent> listener) {
        messageReceivedListeners.add(listener);
    }

    /**
     * 注册消息确认事件监听器。
     *
     * @param listener 消息确认事件消费者
     */
    public void onMessageAcknowledged(Consumer<MessageAcknowledgedEvent> listener) {
        messageAcknowledgedListeners.add(listener);
    }

    /**
     * 触发消息接收事件。
     *
     * @param event 消息接收事件
     */
    protected void raiseMessageReceived(MessageReceivedEvent event) {
        for (Consumer<MessageReceivedEvent> listener : messageReceivedListeners) {
            listener.accept(event);
        }
    }

    /**
     * 触发消息确认事件。
     *
     * @param event 消息确认事件
     */
    protected void raiseMessageAcknowledged(MessageAcknowledgedEvent event) {
        for (Consumer<MessageAcknowledgedEvent> listener : messageAcknowledgedListeners) {
            listener.accept(event);
        }
    }

    /**
     * 启动接收者，开始监听指定通道的消息。
     *
     * @param channelName 通道名称
     */
    abstract void start(String channelName);

    /**
     * 返回接收者名称，默认为当前类的简单名称。
     *
     * @return 接收者名称
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 关闭接收者，记录关闭日志。
     */
    @Override
    public void close() {
        LOGGER.info(() -> "Closing Kafka recipient: " + getName());
    }
}
