package com.euonia.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.reflection.ServiceProvider;
import com.euonia.reflection.SyntheticParameterizedType;

/**
 * Kafka 消息接收者注册器，负责根据已注册的 {@link HandlerRegistration} 列表创建并启动
 * 对应的 Kafka 消费者实例。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class KafkaRecipientRegistrar implements RecipientRegistrar {

    private static final Logger LOGGER = Logger.getLogger(KafkaRecipientRegistrar.class.getName());

    private final ServiceProvider provider;
    private final KafkaBusOptions options;
    private final MessageConvention convention;
    private final TransportStrategy strategy;
    private final List<KafkaRecipient> recipients = new ArrayList<>();

    public KafkaRecipientRegistrar(Configurator configurator, ServiceProvider provider, KafkaBusOptions options) {
        this.provider = provider;
        this.options = options;
        this.convention = configurator.getConventionBuilder().getConvention();
        var strategyBuilder = configurator.getStrategyBuilders().get(options.getName());
        this.strategy = strategyBuilder != null ? strategyBuilder.getStrategy() : null;
    }

    @Override
    public void register(List<HandlerRegistration> registrations, String defaultTransport) {
        var isDefaultTransport = Objects.equals(defaultTransport, options.getName());
        var handlerContext = provider.getService(HandlerContext.class).orElseThrow();
        @SuppressWarnings("null")
        var serializer = provider.getService(MessageSerializer.class).orElseThrow();

        for (var registration : registrations) {
            if (!isDefaultTransport && (strategy == null || !strategy.incoming(registration.messageType()))) {
                continue;
            }

            var syntheticType = SyntheticParameterizedType.withGenerics(RoutedMessage.class, registration.messageType());
            KafkaRecipient recipient;

            if (convention.isMulticastType(registration.messageType())) {
                recipient = new KafkaTopicSubscriber(options, handlerContext, serializer, syntheticType);
            } else if (convention.isUnicastType(registration.messageType())) {
                recipient = new KafkaQueueConsumer(options, handlerContext, serializer, syntheticType);
            } else if (convention.isRequestType(registration.messageType())) {
                recipient = new KafkaRequestExecutor(options, handlerContext, serializer, syntheticType);
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + registration.messageType());
            }

            recipient.start(registration.channel());
            recipients.add(recipient);
        }
    }

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
