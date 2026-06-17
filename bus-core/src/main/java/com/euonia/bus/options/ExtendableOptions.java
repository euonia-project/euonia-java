package com.euonia.bus.options;

/**
 * 消息发送和接收的默认选项，用户可以扩展此类以添加自定义选项。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class ExtendableOptions {
    private String messageId;
    private String channel;
    private String queue;
    private int priority;
    private String requestTraceId;
    private boolean enablePipelineBehaviors = true;
    private boolean attachDefaultPipelineBehaviors = true;

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
     * 获取请求追踪标识符，可用于追踪请求流程。
     *
     * @return 请求追踪标识符
     */
    public String getRequestTraceId() {
        return requestTraceId;
    }

    /**
     * 设置请求追踪标识符，可用于追踪请求流程。
     *
     * @param requestTraceId 请求追踪标识符
     */
    public void setRequestTraceId(String requestTraceId) {
        this.requestTraceId = requestTraceId;
    }

    /**
     * 检查管道行为是否已启用，可用于自定义消息处理。
     *
     * @return 如果管道行为已启用则返回 true，否则返回 false
     */
    public boolean isEnablePipelineBehaviors() {
        return enablePipelineBehaviors;
    }

    /**
     * 设置管道行为是否启用，可用于自定义消息处理。
     *
     * @param enablePipelineBehaviors true 表示启用管道行为，false 表示禁用
     */
    public void setEnablePipelineBehaviors(boolean enablePipelineBehaviors) {
        this.enablePipelineBehaviors = enablePipelineBehaviors;
    }

    /**
     * 检查默认管道行为是否已附加，可用于自定义消息处理。
     *
     * @return 如果默认管道行为已附加则返回 true，否则返回 false
     */
    public boolean isAttachDefaultPipelineBehaviors() {
        return attachDefaultPipelineBehaviors;
    }

    /**
     * 设置默认管道行为是否附加，可用于自定义消息处理。
     *
     * @param attachDefaultPipelineBehaviors true 表示附加默认管道行为，false 表示不附加
     */
    public void setAttachDefaultPipelineBehaviors(boolean attachDefaultPipelineBehaviors) {
        this.attachDefaultPipelineBehaviors = attachDefaultPipelineBehaviors;
    }
}
