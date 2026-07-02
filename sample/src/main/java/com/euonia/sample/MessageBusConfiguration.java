package com.euonia.sample;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.euonia.bus.*;
import com.euonia.bus.Transport;
import com.euonia.bus.convention.AnnotationMessageConvention;
import com.euonia.bus.convention.DefaultMessageConvention;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.bus.strategy.AnnotationTransportStrategy;
import com.euonia.core.Singleton;
import com.euonia.reflection.ServiceProvider;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 消息总线示例的 Spring 配置类，负责注册消息总线所需的所有 Bean。
 * <p>
 * 配置内容包括：
 * <ul>
 *   <li>{@link Configurator} —— 消息约定、传输策略和处理器注册</li>
 *   <li>内存传输（InMemory） —— 用于 Command 消息的本地传输</li>
 *   <li>RabbitMQ 传输 —— 用于 Eto 消息的分布式传输</li>
 *   <li>{@link MessageSerializer} —— 基于 Jackson 的 JSON 序列化器</li>
 *   <li>RabbitMQ 连接工厂</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class MessageBusConfiguration {
    /**
     * Spring 环境抽象，用于读取应用配置属性
     */
    private final Environment environment;

    /**
     * 使用指定的环境构造配置实例。
     *
     * @param environment Spring 环境抽象
     */
    public MessageBusConfiguration(Environment environment) {
        this.environment = environment;
    }

    /**
     * 创建并配置 {@link Configurator} Bean。
     * <p>
     * 配置包括：
     * <ul>
     *   <li>添加默认消息约定和基于注解的消息约定</li>
     *   <li>按命名约定自动识别单播（*Command）、多播（*Event/*Eto）和请求（*Query）类型</li>
     *   <li>为内存传输和 RabbitMQ 传输分别配置传输策略</li>
     *   <li>扫描 {@code application.handler} 包下的处理器</li>
     * </ul>
     *
     * @return 配置好的 {@link Configurator} 实例
     */
    @Bean
    public Configurator configurator() {

        var packageName = this.getClass().getPackageName();

        return new DefaultConfigurator().setConvention(c -> {
                                            c.add(DefaultMessageConvention.class);
                                            c.add(AnnotationMessageConvention.class);
                                            c.evaluateUnicast(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Command"));
                                            c.evaluateMulticast(t -> t.getPackageName().startsWith(packageName) && (t.getSimpleName().endsWith("Event") || t.getSimpleName().endsWith("Eto")));
                                            c.evaluateRequest(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Query"));
                                        })
                                        .setStrategy("InMemoryMessageBusTransport", s -> {
                                            s.add(new AnnotationTransportStrategy("InMemoryMessageBusTransport"));
                                            s.evaluateOutgoing(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Command"));
                                            s.evaluateIncoming(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Command"));
                                        })
                                        .setStrategy("RabbitMqMessageBusTransport", s -> {
                                            s.add(new AnnotationTransportStrategy("RabbitMqMessageBusTransport"));
                                            s.evaluateIncoming(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Eto"));
                                            s.evaluateOutgoing(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Eto"));
                                        })
                                        .registerHandler(packageName + ".application.handler")
                                        .setDefaultTransport(() -> environment.getProperty("euonia.bus.default-transport", "InMemoryMessageBusTransport"))
                                        .setEnablePipelineBehaviors(() -> environment.getProperty("euonia.bus.enable-pipeline-behaviors", Boolean.class, true));
    }

    /**
     * 创建内存传输 Bean，用于本地消息传递。
     *
     * @return 基于内存的 {@link Transport} 实现
     */
    @Bean(name = "InMemoryMessageBusTransport")
    public Transport inMemoryTransport() {
        return new InMemoryTransport(getInMemoryBusOptions());
    }

    /**
     * 创建内存接收者注册器 Bean，用于注册本地消息处理器。
     *
     * @param configurator 消息总线配置器
     * @param provider     服务提供者
     * @return 基于内存的 {@link RecipientRegistrar} 实现
     */
    @Bean(name = "InMemoryRecipientRegistrar")
    public RecipientRegistrar inMemoryRecipientRegistrar(Configurator configurator, ServiceProvider provider) {
        return new InMemoryRecipientRegistrar(configurator, provider, getInMemoryBusOptions());
    }

    /**
     * 创建 RabbitMQ 传输 Bean，用于分布式消息传递。
     *
     * @return 基于 RabbitMQ 的 {@link Transport} 实现
     */
    @Bean(name = "RabbitMqMessageBusTransport")
    public Transport rabbitMqTransport() {
        var options = getRabbitMqBusOptions();
        return new RabbitMqTransport(getRabbitMqConnection(), messageSerializer(), options);
    }

    /**
     * 创建 RabbitMQ 连接 Bean。
     * <p>
     * 基于 {@link RabbitMqBusOptions} 中的配置（主机、端口、用户名、密码、虚拟主机）
     * 创建与 RabbitMQ 代理的连接。
     *
     * @return RabbitMQ {@link Connection} 实例
     * @throws RuntimeException 如果连接创建失败
     */
    @Bean
    public Connection getRabbitMqConnection() {
        try {
            var options = getRabbitMqBusOptions();

            var factory = new ConnectionFactory();
            factory.setHost(options.getHost());
            factory.setPort(options.getPort());
            factory.setUsername(options.getUsername());
            factory.setPassword(options.getPassword());
            factory.setVirtualHost(options.getVirtualHost());
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to create RabbitMQ connection", e);
        }
    }

    /**
     * 创建 RabbitMQ 接收者注册器 Bean，用于注册分布式消息处理器。
     *
     * @param configurator 消息总线配置器
     * @param provider     服务提供者
     * @return 基于 RabbitMQ 的 {@link RecipientRegistrar} 实现
     */
    @Bean(name = "RabbitMqRecipientRegistrar")
    public RecipientRegistrar rabbitMqRecipientRegistrar(Configurator configurator, ServiceProvider provider) {
        return new RabbitMqRecipientRegistrar(configurator, provider, getRabbitMqBusOptions());
    }

    /**
     * 创建消息序列化器 Bean，基于 Jackson ObjectMapper 进行 JSON 序列化。
     * <p>
     * 支持按 {@link Class} 和 {@link java.lang.reflect.Type} 两种方式反序列化，
     * 以及字节数组的序列化/反序列化。
     *
     * @return 基于 Jackson 的 {@link MessageSerializer} 实现
     */
    @Bean
    public MessageSerializer messageSerializer() {
        ObjectMapper mapper = new ObjectMapper();

        return new MessageSerializer() {
            @Override
            public <M> String serialize(M message) {
                try {
                    return mapper.writeValueAsString(message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize message", e);
                }
            }

            @Override
            public <M> M deserialize(String data, Class<M> type) {
                try {
                    return mapper.readValue(data, type);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to deserialize message", e);
                }
            }

            @Override
            public <M> M deserialize(String data, java.lang.reflect.Type type) {
                try {
                    JavaType javaType = mapper.getTypeFactory().constructType(type);
                    return mapper.readValue(data, javaType);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to deserialize message", e);
                }
            }

            @Override
            public Object deserialize(String data) {
                try {
                    return mapper.readValue(data, Object.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to deserialize message", e);
                }
            }

            @Override
            public <M> byte[] serializeToBytes(M message) {
                try {
                    return mapper.writeValueAsBytes(message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize message to bytes", e);
                }
            }

            @Override
            public <M> M deserializeFromBytes(byte[] data, Class<M> type) {
                try {
                    return mapper.readValue(data, type);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to deserialize message from bytes", e);
                }
            }

            @Override
            public Object deserializeFromBytes(byte[] data) {
                try {
                    return mapper.readValue(data, Object.class);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to deserialize message from bytes", e);
                }
            }

            @Override
            public <V> String serializeEnvelope(MessageEnvelope<V> envelope) {
                try {
                    return mapper.writeValueAsString(envelope);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize message envelope", e);
                }
            }

            @Override
            public <V> MessageEnvelope<V> deserializeEnvelope(String data, Class<V> payloadType) {
                var javaType = mapper.getTypeFactory().constructParametricType(RoutedMessage.class, payloadType);
                return deserialize(data, javaType);
            }
        };
    }

    /**
     * 获取内存总线选项的单例实例，从应用配置中读取相关属性。
     *
     * @return 配置好的 {@link InMemoryBusOptions} 实例
     */
    private InMemoryBusOptions getInMemoryBusOptions() {
        return Singleton.get(InMemoryBusOptions.class, () -> {
            var options = new InMemoryBusOptions();
            options.setName(environment.getProperty("euonia.bus.inmemory.name", String.class, "InMemoryMessageBusTransport"));
            options.setLazyInitialize(environment.getProperty("euonia.bus.lazy-initialize", Boolean.class, false));
            return options;
        });
    }

    /**
     * 获取 RabbitMQ 总线选项的单例实例，从应用配置中读取所有 RabbitMQ 相关属性。
     * <p>
     * 配置属性前缀为 {@code euonia.bus.rabbitmq.*}，包括主机、端口、重试策略等。
     * 订阅 ID 从 {@code spring.application.name} 读取。
     *
     * @return 配置好的 {@link RabbitMqBusOptions} 实例
     */
    @SuppressWarnings("null")
    private RabbitMqBusOptions getRabbitMqBusOptions() {
        return Singleton.get(RabbitMqBusOptions.class, () -> {
            var options = new RabbitMqBusOptions();

            options.setEnabled(() -> environment.getProperty("euonia.bus.rabbitmq.enabled", Boolean.class, true));

            getEnvironmentProperty("euonia.bus.rabbitmq.name", String.class, "RabbitMqMessageBusTransport", options::setName);
            getEnvironmentProperty("euonia.bus.rabbitmq.host", String.class, options::setHost);
            getEnvironmentProperty("euonia.bus.rabbitmq.port", Integer.class, options::setPort);
            getEnvironmentProperty("euonia.bus.rabbitmq.username", String.class, options::setUsername);
            getEnvironmentProperty("euonia.bus.rabbitmq.password", String.class, options::setPassword);
            getEnvironmentProperty("euonia.bus.rabbitmq.virtualHost", String.class, options::setVirtualHost);
            getEnvironmentProperty("euonia.bus.rabbitmq.connection", String.class, options::setConnection);
            getEnvironmentProperty("spring.application.name", String.class, "euonia-java-sample", options::setSubscriptionId);
            getEnvironmentProperty("euonia.bus.rabbitmq.exchange-name-prefix", String.class, options::setExchangeNamePrefix);
            getEnvironmentProperty("euonia.bus.rabbitmq.queue-name-prefix", String.class, options::setQueueNamePrefix);
            getEnvironmentProperty("euonia.bus.rabbitmq.rpc-queue-prefix", String.class, options::setRpcQueuePrefix);
            getEnvironmentProperty("euonia.bus.rabbitmq.mandatory", Boolean.class, true, options::setMandatory);
            getEnvironmentProperty("euonia.bus.rabbitmq.prefetch-count", Integer.class, 1, options::setPrefetchCount);
            getEnvironmentProperty("euonia.bus.rabbitmq.retry-delay", Long.class, 5000L, options::setRetryDelay);
            getEnvironmentProperty("euonia.bus.rabbitmq.retry-attempts", Integer.class, 3, options::setRetryAttempts);

            return options;
        });
    }

    /**
     * 从环境中读取属性值，如果值存在则传递给指定的消费者。
     *
     * @param <T>        属性类型
     * @param key        属性键
     * @param targetType 目标类型
     * @param next       接收属性值的消费者
     */
    private <T> void getEnvironmentProperty(String key, Class<T> targetType, Consumer<T> next) {
        var value = environment.getProperty(key, targetType);
        if (value != null && next != null) {
            next.accept(value);
        }
    }

    /**
     * 从环境中读取属性值，如果值不存在则使用默认值，然后传递给指定的消费者。
     *
     * @param <T>          属性类型
     * @param key          属性键
     * @param targetType   目标类型
     * @param defaultValue 默认值
     * @param next         接收属性值的消费者
     */
    private <T> void getEnvironmentProperty(String key, Class<T> targetType, T defaultValue, Consumer<T> next) {
        T value = environment.getProperty(key, targetType, defaultValue);
        if (next != null) {
            next.accept(value);
        }
    }
}
