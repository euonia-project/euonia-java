package com.euonia.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.exception.MessageConventionException;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.reflection.ServiceProvider;

/**
 * Kafka 消息接收者注册器，负责根据已注册的 {@link ChannelRegistration} 列表创建并启动
 * 对应的 Kafka 消费者实例。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class KafkaRecipientRegistrar implements RecipientRegistrar {

    private static final Logger LOGGER = Logger.getLogger(KafkaRecipientRegistrar.class.getName());

    /** 服务提供者 */
    private final ServiceProvider provider;
    /** Kafka 总线选项 */
    private final KafkaBusOptions options;
    /** 消息约定 */
    private final MessageConvention convention;
    /** 传输策略 */
    private final TransportStrategy strategy;
    /** 已创建的接收者列表 */
    private final List<KafkaRecipient> recipients = new ArrayList<>();

    /**
     * 使用配置器、服务提供者和 Kafka 选项构造注册器。
     *
     * @param configurator 消息总线配置器
     * @param provider     服务提供者
     * @param options      Kafka 总线选项
     */
    public KafkaRecipientRegistrar(Configurator configurator, ServiceProvider provider, KafkaBusOptions options) {
        this.provider = provider;
        this.options = options;
        this.convention = configurator.getConventionBuilder().getConvention();
        var strategyBuilder = configurator.getStrategyBuilders().get(options.getName());
        this.strategy = strategyBuilder != null ? strategyBuilder.getStrategy() : null;
    }

    @Override
    public void register(Map<String, ChannelRegistration> registrations, String defaultTransport) {
        var isDefaultTransport = Objects.equals(defaultTransport, options.getName());
        var handlerContext = provider.getService(HandlerContext.class).orElseThrow();
        @SuppressWarnings("null")
        var serializer = provider.getService(MessageSerializer.class).orElseThrow();

        for (var entry : registrations.entrySet()) {
            var channel = entry.getKey();
            var messageType = entry.getValue().getMessageType();

            if (!isDefaultTransport && (strategy == null || !strategy.allowIncoming(channel, messageType))) {
                continue;
            }

            KafkaRecipient recipient;

            if (convention.isMulticast(channel)) {
                recipient = new KafkaTopicSubscriber(options, handlerContext, serializer, messageType);
            } else if (convention.isUnicast(channel)) {
                recipient = new KafkaQueueConsumer(options, handlerContext, serializer, messageType);
            } else if (convention.isRequest(channel)) {
                recipient = new KafkaRequestExecutor(options, handlerContext, serializer, messageType);
            } else {
                throw new MessageConventionException("Unsupported message type: " + messageType);
            }

            recipient.start(channel);
            recipients.add(recipient);
        }
    }

    /**
     * 关闭所有已注册的接收者。
     */
    public void close() {
        for (var recipient : recipients) {
            try {
                recipient.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, () -> "Error closing recipient '" + recipient.getName() + "': " + e.getMessage());
            }
        }
        recipients.clear();
    }
}
