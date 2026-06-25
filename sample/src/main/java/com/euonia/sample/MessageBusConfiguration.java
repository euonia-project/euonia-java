package com.euonia.sample;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.euonia.bus.*;
import com.euonia.bus.contract.Transport;
import com.euonia.bus.convention.AnnotationMessageConvention;
import com.euonia.bus.convention.DefaultMessageConvention;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.bus.strategy.AnnotationTransportStrategy;
import com.euonia.core.Singleton;
import com.euonia.reflection.ServiceProvider;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class MessageBusConfiguration {
    private final Environment environment;

    @Value("${euonia.bus.default-transport:InMemoryMessageBusTransport}")
    private String defaultTransport;

    public MessageBusConfiguration(Environment environment) {
        this.environment = environment;
    }

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
                                        .registerHandlers(packageName + ".application.handler");
    }

    @Bean(name = "InMemoryMessageBusTransport")
    public Transport inMemoryTransport() {
        return new InMemoryTransport(getInMemoryBusOptions());
    }

    @Bean(name = "InMemoryRecipientRegistrar")
    public RecipientRegistrar inMemoryRecipientRegistrar(Configurator configurator, ServiceProvider provider) {
        return new InMemoryRecipientRegistrar(configurator, provider, getInMemoryBusOptions());
    }

    @Bean(name = "RabbitMqMessageBusTransport")
    public Transport rabbitMqTransport() {
        var options = getRabbitMqBusOptions();
        return new RabbitMqTransport(getRabbitMqConnection(), messageSerializer(), options);
    }

    @Bean
    public Connection getRabbitMqConnection() {
        try {
            var options = getRabbitMqBusOptions();

            var connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(options.getHost());
            connectionFactory.setPort(options.getPort());
            connectionFactory.setUsername(options.getUsername());
            connectionFactory.setPassword(options.getPassword());
            connectionFactory.setVirtualHost(options.getVirtualHost());
            return connectionFactory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to create RabbitMQ connection", e);
        }
    }

    @Bean(name = "RabbitMqRecipientRegistrar")
    public RecipientRegistrar rabbitMqRecipientRegistrar(Configurator configurator, ServiceProvider provider) {
        return new RabbitMqRecipientRegistrar(configurator, provider, getRabbitMqBusOptions());
    }

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
        };
    }

    private InMemoryBusOptions getInMemoryBusOptions() {
        return Singleton.get(InMemoryBusOptions.class, () -> {
            var options = new InMemoryBusOptions();
            options.setName(environment.getProperty("euonia.bus.inmemory.name", String.class, "InMemoryMessageBusTransport"));
            options.setLazyInitialize(environment.getProperty("euonia.bus.lazy-initialize", Boolean.class, false));
            return options;
        });
    }

    private RabbitMqBusOptions getRabbitMqBusOptions() {
        return Singleton.get(RabbitMqBusOptions.class, () -> {
            var options = new RabbitMqBusOptions();
            options.setName(environment.getProperty("euonia.bus.rabbitmq.name", String.class, "RabbitMqMessageBusTransport"));
            options.setHost(environment.getProperty("euonia.bus.rabbitmq.host", String.class, "localhost"));
            options.setPort(environment.getProperty("euonia.bus.rabbitmq.port", Integer.class, 5672));
            options.setUsername(environment.getProperty("euonia.bus.rabbitmq.username", String.class, "guest"));
            options.setPassword(environment.getProperty("euonia.bus.rabbitmq.password", String.class, "guest"));
            options.setVirtualHost(environment.getProperty("euonia.bus.rabbitmq.virtualHost", String.class, "/"));
            return options;
        });
    }
}
