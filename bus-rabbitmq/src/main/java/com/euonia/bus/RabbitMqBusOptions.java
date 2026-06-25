package com.euonia.bus;

import com.euonia.utility.Assert;

/**
 * RabbitMQ 总线传输的配置选项。
 * 该类封装了与 RabbitMQ 建立连接所需的所有必要设置，以及消息处理的各种参数，如交换器和队列配置、重试策略等。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RabbitMqBusOptions {
    /**
     * 是否启用该传输
     */
    private boolean enabled = true;
    /**
     * 传输名称
     */
    private String name = RabbitMqTransport.class.getSimpleName();
    /**
     * 主机地址
     */
    private String host = "localhost", username = "guest", password = "guest", virtualHost = "/", connection;
    /**
     * 端口号
     */
    private int port = 5672;

    /**
     * 交换器名称前缀
     */
    private String exchangeNamePrefix = Constants.DEFAULT_EXCHANGE_NAME_PREFIX;
    private String queueNamePrefix = Constants.DEFAULT_QUEUE_NAME_PREFIX;
    private String rpcQueuePrefix = Constants.DEFAULT_RPC_QUEUE_NAME_PREFIX;

    /**
     * 是否为强制模式
     */
    private boolean mandatory = true;

    /**
     * 预取计数
     */
    private int prefetchCount = 1;
    /**
     * 重试延迟（毫秒）
     */
    private long retryDelay = 5000L;
    /**
     * 重试次数
     */
    private int retryAttempts = 3;

    /**
     * 订阅标识符
     */
    private String subscriptionId;

    /**
     * 检查是否启用该传输。
     *
     * @return 如果启用则返回 true，否则返回 false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用该传输。
     *
     * @param enabled 如果启用则设置为 true，否则设置为 false
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
     * 获取主机地址。
     *
     * @return 主机地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置主机地址。
     *
     * @param host 主机地址
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取端口号。
     *
     * @return 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置端口号。
     *
     * @param port 端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码。
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码。
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取虚拟主机。
     *
     * @return 虚拟主机
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * 设置虚拟主机。
     *
     * @param virtualHost 虚拟主机
     */
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    /**
     * 构建并返回 RabbitMQ 连接字符串。
     * <p>
     * 如果显式设置了 {@code connection} 属性，则直接返回；
     * 否则基于 host、port、username、password 和 virtualHost 属性自动构建 AMQP 连接字符串。
     *
     * @return AMQP 连接字符串
     */
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

    /**
     * 设置 RabbitMQ 连接字符串。
     *
     * @param connection AMQP 连接字符串
     */
    public void setConnection(String connection) {
        this.connection = connection;
    }

    /**
     * 获取交换器名称前缀。
     *
     * @return 交换器名称前缀
     */
    public String getExchangeNamePrefix() {
        return exchangeNamePrefix;
    }

    /**
     * 设置交换器名称前缀。
     *
     * @param exchangeNamePrefix 交换器名称前缀
     */
    public void setExchangeNamePrefix(String exchangeNamePrefix) {
        this.exchangeNamePrefix = exchangeNamePrefix;
    }

    /**
     * 获取队列名称前缀。
     *
     * @return 队列名称前缀
     */
    public String getQueueNamePrefix() {
        return queueNamePrefix;
    }

    /**
     * 设置队列名称前缀。
     *
     * @param queueNamePrefix 队列名称前缀
     */
    public void setQueueNamePrefix(String queueNamePrefix) {
        this.queueNamePrefix = queueNamePrefix;
    }

    /**
     * 获取 RPC 队列前缀。
     *
     * @return RPC 队列前缀
     */
    public String getRpcQueuePrefix() {
        return rpcQueuePrefix;
    }

    /**
     * 设置 RPC 队列前缀。
     *
     * @param rpcQueuePrefix RPC 队列前缀
     */
    public void setRpcQueuePrefix(String rpcQueuePrefix) {
        this.rpcQueuePrefix = rpcQueuePrefix;
    }

    /**
     * 是否为强制模式。
     *
     * @return 是否为强制模式
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * 设置是否为强制模式。
     *
     * @param mandatory 是否为强制模式
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * 获取预取计数。
     *
     * @return 预取计数
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * 设置预取计数。
     *
     * @param prefetchCount 预取计数
     */
    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    /**
     * 获取重试延迟时间。
     *
     * @return 重试延迟时间
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * 设置重试延迟时间。
     *
     * @param retryDelay 重试延迟时间
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * 获取重试次数。
     *
     * @return 重试次数
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * 设置重试次数。
     *
     * @param retryAttempts 重试次数
     */
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    /**
     * 获取订阅 ID。
     *
     * @return 订阅 ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * 设置订阅 ID。
     *
     * @param subscriptionId 订阅 ID
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * 生成队列名称，格式为：{prefix}:{channelName}@{subscriptionId}。
     *
     * @param prefix      队列前缀
     * @param channelName 通道名称
     * @return 生成的队列名称
     */
    String generateQueueName(String prefix, String channelName) {
        Assert.notNull(prefix, "prefix cannot be null");
        Assert.notNull(channelName, "channelName cannot be null");
        Assert.notNull(subscriptionId, "subscriptionId cannot be null");
        return String.format("%s:%s@%s", prefix, channelName, subscriptionId);
    }
}
