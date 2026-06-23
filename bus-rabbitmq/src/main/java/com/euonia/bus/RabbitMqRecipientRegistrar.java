package com.euonia.bus;

import java.util.List;
import java.util.Objects;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.reflection.ServiceProvider;
import com.euonia.reflection.SyntheticParameterizedType;
import com.rabbitmq.client.Connection;

/**
 * RabbitMQ 消息接收者注册器，负责根据已注册的 {@link HandlerRegistration} 列表创建并启动
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

    /** 服务提供者，用于解析依赖服务 */
    private final ServiceProvider provider;

    /** RabbitMQ 总线选项 */
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
        this.strategy = configurator.getStrategyBuilders().get(options.getName()).getStrategy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(List<HandlerRegistration> registrations, String defaultTransport) {
        var isDefaultTransport = Objects.equals(defaultTransport, options.getName());

        var connection = provider.getService(Connection.class)
                                 .orElseThrow();
        var handlerContext = provider.getService(HandlerContext.class)
                                     .orElseThrow();
        var serializer = provider.getService(MessageSerializer.class)
                                 .orElseThrow();

        for (var registration : registrations) {
            if (!isDefaultTransport && (strategy == null || !strategy.incoming(registration.messageType()))) {
                /* 如果此注册不是针对默认传输，且策略未将其标识为入站类型，则跳过。 */
                continue;
            }

            /* 构造 RoutedMessage<MessageType> 的合成参数化类型以获取目标类 */
            var syntheticType = SyntheticParameterizedType.withGenerics(RoutedMessage.class, registration.messageType());
            var destinationType = (Class<? extends RoutedMessage<?>>) syntheticType.getRawType();

            RabbitMqRecipient recipient;

            if (convention.isMulticastType(registration.messageType())) {
                recipient = new RabbitMqTopicSubscriber(connection, options, handlerContext, serializer, destinationType);
            } else if (convention.isUnicastType(registration.messageType())) {
                recipient = new RabbitMqQueueConsumer(connection, options, handlerContext, serializer, destinationType);
            } else if (convention.isRequestType(registration.messageType())) {
                recipient = new RabbitMqRequestExecutor(connection, options, handlerContext, serializer, destinationType);
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + registration.messageType());
            }
            recipient.start(registration.channel());
        }
    }
}
