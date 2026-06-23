package com.euonia.bus;

/**
 * Configuration options for the RabbitMQ bus transport.
 * This class encapsulates all the necessary settings required to establish a connection with RabbitMQ,
 * as well as various parameters for message handling, such as exchange and queue configurations, retry policies, and more.
 */
public final class RabbitMqBusOptions {
    private boolean enabled = true;
    private String name = RabbitMqTransport.class.getSimpleName();
    private String host = "localhost", username = "guest", password = "guest", virtualHost = "/", connection;
    private int port = 5672;

    private String exchangeNamePrefix = Constants.DEFAULT_EXCHANGE_NAME_PREFIX,
        queueNamePrefix = Constants.DEFAULT_QUEUE_NAME_PREFIX,
        exchangeType = Constants.DEFAULT_EXCHANGE_TYPE,
        topicName = Constants.DEFAULT_TOPIC_NAME_PREFIX,
        rpcQueuePrefix = Constants.DEFAULT_RPC_QUEUE_NAME_PREFIX,
        routingKey = "*";

    private boolean autoAck = true, persistent = true, mandatory = true;

    private int prefetchCount = 1;
    private long retryDelay = 5000L;
    private int retryAttempts = 3;

    private String subscriptionId;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getConnection() {
        if (connection == null || connection.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("amqp://");
            if (username != null && !username.isEmpty()) {
                sb.append(username);
                if (password != null && !password.isEmpty()) {
                    sb.append(":").append(password);
                }
                sb.append("@");
            }
            sb.append(host).append(":").append(port);
            if (virtualHost != null && !virtualHost.isEmpty()) {
                sb.append("/").append(virtualHost);
            }
            return sb.toString();
        } else {
            return connection;
        }
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getExchangeNamePrefix() {
        return exchangeNamePrefix;
    }

    public void setExchangeNamePrefix(String exchangeNamePrefix) {
        this.exchangeNamePrefix = exchangeNamePrefix;
    }

    public String getQueueNamePrefix() {
        return queueNamePrefix;
    }

    public void setQueueNamePrefix(String queueNamePrefix) {
        this.queueNamePrefix = queueNamePrefix;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getRpcQueuePrefix() {
        return rpcQueuePrefix;
    }

    public void setRpcQueuePrefix(String rpcQueuePrefix) {
        this.rpcQueuePrefix = rpcQueuePrefix;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
