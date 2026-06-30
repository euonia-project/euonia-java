package com.euonia.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.reflection.ServiceProvider;
import com.euonia.reflection.SyntheticParameterizedType;
import com.euonia.utility.Assert;
import com.rabbitmq.client.Connection;

/**
 * RabbitMQ 消息接收者注册器，负责根据已注册的 {@link ChannelRegistration} 列表创建并启动
 * 对应的 RabbitMQ 消费者实例。
 * <p>
 * 根据消息约定判断消息类型，分别创建：
 * <ul>
 *   <li>多播类型 —— {@link RabbitMqTopicSubscriber}</li>
 *   <li>单播类型 —— {@link RabbitMqQueueConsumer}</li>
 *   <li>请求类型 —— {@link RabbitMqRequestExecutor}</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RabbitMqRecipientRegistrar implements RecipientRegistrar {

    private static final Logger LOGGER = Logger.getLogger(RabbitMqRecipientRegistrar.class.getName());

    /**
     * 已创建的接收者列表，用于生命周期管理
     */
    private final List<RabbitMqRecipient> recipients = new ArrayList<>();

    /**
     * 服务提供者，用于解析依赖服务
     */
    private final ServiceProvider provider;

    /**
     * RabbitMQ 总线选项
     */
    private final RabbitMqBusOptions options;

    /**
     * 消息约定，用于判断消息类型（单播/多播/请求）。
     */
    private final MessageConvention convention;

    /**
     * 传输策略，用于判断消息是否应由当前传输实例处理。
     */
    private final TransportStrategy strategy;

    /**
     * 使用指定的配置器、服务提供者和 RabbitMQ 选项构造注册器。
     *
     * @param configurator 用于获取约定和策略的配置器
     * @param provider     服务提供者
     * @param options      RabbitMQ 总线选项
     */
    public RabbitMqRecipientRegistrar(Configurator configurator, ServiceProvider provider, RabbitMqBusOptions options) {
        this.provider = provider;
        this.options = options;
        this.convention = configurator.getConventionBuilder().getConvention();
        var strategyBuilder = configurator.getStrategyBuilders().get(options.getName());
        Assert.notNull(strategyBuilder, () -> "No strategy found for transport '" + options.getName() + "'");
        this.strategy = strategyBuilder.getStrategy();
    }

    @Override
    public void register(Map<String, ChannelRegistration> registrations, String defaultTransport) {
        var isDefaultTransport = Objects.equals(defaultTransport, options.getName());

        var connection = provider.getService(Connection.class)
                                 .orElseThrow();
        var handlerContext = provider.getService(HandlerContext.class)
                                     .orElseThrow();
        var serializer = provider.getService(MessageSerializer.class)
                                 .orElseThrow();

        for (var entry : registrations.entrySet()) {
            var channel = entry.getKey();
            var messageType = entry.getValue().getMessageType();

            if (!isDefaultTransport && (strategy == null || !strategy.incoming(messageType))) {
                /* 如果此注册不是针对默认传输，且策略未将其标识为入站类型，则跳过。 */
                continue;
            }

            /* 构造 RoutedMessage<MessageType> 的合成参数化类型以获取目标类型 */
            var syntheticType = SyntheticParameterizedType.withGenerics(RoutedMessage.class, messageType);

            RabbitMqRecipient recipient;

            if (convention.isMulticastType(messageType)) {
                recipient = new RabbitMqTopicSubscriber(connection, options, handlerContext, serializer, syntheticType);
            } else if (convention.isUnicastType(messageType)) {
                recipient = new RabbitMqQueueConsumer(connection, options, handlerContext, serializer, syntheticType);
            } else if (convention.isRequestType(messageType)) {
                recipient = new RabbitMqRequestExecutor(connection, options, handlerContext, serializer, syntheticType);
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + messageType);
            }
            recipient.onMessageReceived(event -> {
                LOGGER.log(Level.INFO, () -> String.format("[%s] Message '%s' received via %s", event.getContext().getRequestTraceId(), event.getContext().getMessageId(), recipient.getName()));
            });
            recipient.onMessageAcknowledged(event -> {
                LOGGER.log(Level.INFO, () -> String.format("[%s] Message '%s' acknowledged via %s", event.getContext().getRequestTraceId(), event.getContext().getMessageId(), recipient.getName()));
            });
            recipient.start(channel);
            recipients.add(recipient);
        }
    }

    /**
     * 关闭所有已创建的接收者，停止消息监听并释放 Channel 资源。
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
