package com.euonia.bus.options;

import com.euonia.bus.MessageMetadata;
import com.euonia.core.GuidType;
import com.euonia.core.ObjectId;

import java.util.function.Consumer;

/**
 * 消息发送和接收的默认选项，用户可以扩展此类以添加自定义选项。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class ExtendableOptions {
    private String messageId = ObjectId.newGuid(GuidType.SEQUENTIAL_AS_STRING).toString();
    private String channel;
    private String queue;
    private int priority;
    private long delay, timeout;
    private Consumer<MessageMetadata> metadataSetter;

    /**
     * 获取消息的唯一标识符，可用于消息追踪和关联。
     *
     * @return 消息的唯一标识符
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * 设置消息的唯一标识符，可用于消息追踪和关联。
     *
     * @param messageId 消息的唯一标识符
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * 获取消息所属的通道名称，可用于消息路由和分类。
     *
     * @return 消息的通道名称
     */
    public String getChannel() {
        return channel;
    }

    /**
     * 设置消息所属的通道名称，可用于消息路由和分类。
     *
     * @param channel 消息的通道名称
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * 获取消息所属的队列名称，可用于消息路由和分类。
     *
     * @return 消息的队列名称
     */
    public String getQueue() {
        return queue;
    }

    /**
     * 设置消息所属的队列名称，可用于消息路由和分类。
     *
     * @param queue 消息的队列名称
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * 获取消息的优先级，可用于消息排序和处理。
     *
     * @return 消息的优先级
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 设置消息的优先级，可用于消息排序和处理。
     *
     * @param priority 消息的优先级
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 获取消息的延迟时间（以毫秒为单位），用于控制消息的发送或处理延迟。
     *
     * @return 消息的延迟时间（毫秒）
     */
    public long getDelay() {
        return delay;
    }

    /**
     * 设置消息的延迟时间（以毫秒为单位），用于控制消息的发送或处理延迟。
     *
     * @param delay 消息的延迟时间（毫秒）
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * 获取消息的超时时间（以毫秒为单位），用于控制消息的处理超时。
     *
     * @return 消息的超时时间（毫秒）
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * 设置消息的超时时间（以毫秒为单位），用于控制消息的处理超时。
     *
     * @param timeout 消息的超时时间（毫秒）
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取用于设置消息元数据的函数。该函数接受一个 {@link MessageMetadata} 对象，并允许用户自定义消息的元数据。
     *
     * @return 用于设置消息元数据的函数
     */
    public Consumer<MessageMetadata> getMetadataSetter() {
        return metadataSetter;
    }

    /**
     * 设置用于设置消息元数据的函数。该函数接受一个 {@link MessageMetadata} 对象，并允许用户自定义消息的元数据。
     *
     * @param metadataSetter 用于设置消息元数据的函数
     */
    public void setMetadataSetter(Consumer<MessageMetadata> metadataSetter) {
        this.metadataSetter = metadataSetter;
    }
}
