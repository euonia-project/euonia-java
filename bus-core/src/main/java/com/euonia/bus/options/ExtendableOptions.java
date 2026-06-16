package com.euonia.bus.options;

/**
 * Default options for message sending and receiving, which can be extended by
 * users to add custom options.
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
     * Get the unique identifier of the message, which can be used for message
     * tracking and correlation.
     *
     * @return the unique identifier of the message
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Set the unique identifier of the message, which can be used for message
     * tracking and correlation.
     *
     * @param messageId the unique identifier of the message
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets the channel name to which the message belongs, which can be used for
     * message routing and categorization.
     *
     * @return the channel name of the message
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the channel name to which the message belongs, which can be used for
     * message routing and categorization.
     *
     * @param channel the channel name of the message
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Gets the queue name to which the message belongs, which can be used for
     * message routing and categorization.
     *
     * @return the queue name of the message
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets the queue name to which the message belongs, which can be used for
     * message routing and categorization.
     *
     * @param queue the queue name of the message
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * Gets the priority of the message, which can be used for message ordering and
     * processing.
     *
     * @return the priority of the message
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the message, which can be used for message ordering and
     * processing.
     *
     * @param priority the priority of the message
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets the request trace identifier, which can be used for tracing the request
     * flow.
     *
     * @return the request trace identifier
     */
    public String getRequestTraceId() {
        return requestTraceId;
    }

    /**
     * Sets the request trace identifier, which can be used for tracing the request
     * flow.
     *
     * @param requestTraceId the request trace identifier
     */
    public void setRequestTraceId(String requestTraceId) {
        this.requestTraceId = requestTraceId;
    }

    /**
     * Checks if pipeline behaviors are enabled, which can be used for message
     * processing customization.
     *
     * @return true if pipeline behaviors are enabled, false otherwise
     */
    public boolean isEnablePipelineBehaviors() {
        return enablePipelineBehaviors;
    }

    /**
     * Sets whether pipeline behaviors are enabled, which can be used for message
     * processing customization.
     *
     * @param enablePipelineBehaviors true to enable pipeline behaviors, false
     *                                otherwise
     */
    public void setEnablePipelineBehaviors(boolean enablePipelineBehaviors) {
        this.enablePipelineBehaviors = enablePipelineBehaviors;
    }

    /**
     * Checks if default pipeline behaviors are attached, which can be used for
     * message processing customization.
     *
     * @return true if default pipeline behaviors are attached, false otherwise
     */
    public boolean isAttachDefaultPipelineBehaviors() {
        return attachDefaultPipelineBehaviors;
    }

    /**
     * Sets whether default pipeline behaviors are attached, which can be used for
     * message processing customization.
     *
     * @param attachDefaultPipelineBehaviors true to attach default pipeline
     *                                       behaviors, false otherwise
     */
    public void setAttachDefaultPipelineBehaviors(boolean attachDefaultPipelineBehaviors) {
        this.attachDefaultPipelineBehaviors = attachDefaultPipelineBehaviors;
    }
}
