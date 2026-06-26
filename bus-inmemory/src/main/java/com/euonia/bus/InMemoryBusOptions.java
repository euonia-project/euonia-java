package com.euonia.bus;

/**
 * 内存消息总线传输的配置选项。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class InMemoryBusOptions {

    /**
     * 默认的传输名称。
     */
    public static final String DEFAULT_TRANSPORT_NAME = "InMemoryTransport";

    /**
     * 传输名称。
     */
    private String name = DEFAULT_TRANSPORT_NAME;
    /**
     * 是否启用延迟初始化。
     */
    private boolean lazyInitialize = true;
    /**
     * 最大并发调用数。
     */
    private int maxConcurrentCalls = 1;
    /**
     * 是否为每个消息通道创建独立的订阅者实例。
     */
    private boolean multipleSubscriberInstance;

    /**
     * 是否启用死信队列。启用后，处理失败的消息将被存储到 {@link InMemoryDeadLetterQueue}。
     */
    private boolean deadLetterEnabled = true;

    /**
     * 获取传输名称。
     *
     * @return 传输名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置传输名称。
     *
     * @param name 传输名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取是否启用延迟初始化。
     *
     * @return 如果启用延迟初始化则返回 {@code true}
     */
    public boolean isLazyInitialize() {
        return lazyInitialize;
    }

    /**
     * 设置是否启用延迟初始化。
     *
     * @param lazyInitialize 延迟初始化标志
     */
    public void setLazyInitialize(boolean lazyInitialize) {
        this.lazyInitialize = lazyInitialize;
    }

    /**
     * 获取最大并发调用数。
     *
     * @return 最大并发调用数
     */
    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     * 设置最大并发调用数。
     *
     * @param maxConcurrentCalls 最大并发调用数
     */
    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    /**
     * 获取是否为每个消息通道创建独立的订阅者实例。
     *
     * @return 如果启用多订阅者实例则返回 {@code true}
     */
    public boolean isMultipleSubscriberInstance() {
        return multipleSubscriberInstance;
    }

    /**
     * 设置是否为每个消息通道创建独立的订阅者实例。
     *
     * @param multipleSubscriberInstance 多订阅者实例标志
     */
    public void setMultipleSubscriberInstance(boolean multipleSubscriberInstance) {
        this.multipleSubscriberInstance = multipleSubscriberInstance;
    }

    public boolean isDeadLetterEnabled() {
        return deadLetterEnabled;
    }

    public void setDeadLetterEnabled(boolean deadLetterEnabled) {
        this.deadLetterEnabled = deadLetterEnabled;
    }
}
