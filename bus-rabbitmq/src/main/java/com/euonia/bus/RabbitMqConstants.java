package com.euonia.bus;

/**
 * RabbitMQ 总线模块的内部常量定义。
 * <p>
 * 定义 RabbitMQ 交换器、队列和 RPC 队列的默认名称前缀。
 * 这些前缀用于在 RabbitMQ 中生成唯一的交换器和队列名称。
 *
 * @author damon(zhaorong@outlook.com)
 */
class RabbitMqConstants {
    /**
     * 默认交换器名称前缀
     */
    static final String DEFAULT_EXCHANGE_NAME_PREFIX = "$nerosoft.euonia.exchange";
    /**
     * 默认队列名称前缀
     */
    static final String DEFAULT_QUEUE_NAME_PREFIX = "$nerosoft.euonia.queue";
    /**
     * 默认 RPC 队列名称前缀
     */
    static final String DEFAULT_RPC_QUEUE_NAME_PREFIX = "$nerosoft.euonia.request";
    /**
     * 死信队列交换器名称前缀
     */
    static final String DEFAULT_DLX_EXCHANGE_PREFIX = "$nerosoft.euonia.dlx";
    /**
     * 死信队列名称前缀
     */
    static final String DEFAULT_DLQ_QUEUE_NAME_PREFIX = "$nerosoft.euonia.dlq";
    /**
     * 死信路由键
     */
    static final String DEFAULT_DLX_ROUTING_KEY = "dead-letter";
}
