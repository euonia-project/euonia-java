package com.euonia.bus;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.euonia.bus.convention.MessageConvention;
import com.euonia.bus.messenger.StrongReferenceMessenger;
import com.euonia.bus.messenger.WeakReferenceMessenger;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.strategy.TransportStrategy;
import com.euonia.reflection.ServiceProvider;

/**
 * 内存消息总线的接收者注册器。
 * <p>
 * 负责将消息处理器注册到对应的内存消息传输实例。根据消息约定（{@link MessageConvention}），
 * 将消息类型分类为单播、多播或请求类型，并分别注册到
 * {@link com.euonia.bus.messenger.StrongReferenceMessenger} 或
 * {@link com.euonia.bus.messenger.WeakReferenceMessenger}。
 *
 * @author damon(zhaorong@outlook)
 */
public class InMemoryRecipientRegistrar implements RecipientRegistrar {

    /**
     * 服务提供者，用于创建接收者实例。
     */
    private final ServiceProvider provider;

    /**
     * 内存总线配置选项。
     */
    private final InMemoryBusOptions options;

    /**
     * 消息约定，用于判断消息类型（单播/多播/请求）。
     */
    private final MessageConvention convention;

    /**
     * 传输策略，用于判断消息是否应由当前传输实例处理。
     */
    private final TransportStrategy strategy;

    /**
     * 接收者注册缓存，避免重复创建（当不允许多实例时）。
     */
    private final ConcurrentMap<Class<?>, InMemoryRecipient> registrationCache = new ConcurrentHashMap<>();

    /**
     * 创建内存接收者注册器。
     *
     * @param configurator 总线配置器，提供约定和策略
     * @param provider     服务提供者，用于创建接收者实例
     * @param options      内存总线配置选项
     */
    public InMemoryRecipientRegistrar(Configurator configurator, ServiceProvider provider, InMemoryBusOptions options) {
        this.provider = provider;
        this.options = options;
        this.convention = configurator.getConventionBuilder().getConvention();
        this.strategy = configurator.getStrategyBuilders().get(options.getName()).getStrategy();
    }

    @Override
    public void register(List<HandlerRegistration> registrations, String defaultTransport) {

        var isDefaultTransport = Objects.equals(defaultTransport, options.getName());

        for (HandlerRegistration registration : registrations) {
            if (!isDefaultTransport && (strategy == null || !strategy.incoming(registration.messageType()))) {
                /* 如果此注册不是针对默认传输，且策略未将其标识为入站类型，则跳过。 */
                continue;
            }

            if (convention.isUnicastType(registration.messageType())) {
                var recipient = getRecipient(InMemoryUnicastRecipient.class);
                StrongReferenceMessenger.getDefault().register(recipient, MessagePack.class, registration.channel());
            } else if (convention.isMulticastType(registration.messageType())) {
                var recipient = getRecipient(InMemoryMulticastRecipient.class);
                WeakReferenceMessenger.getDefault().register(recipient, MessagePack.class, registration.channel());
            } else if (convention.isRequestType(registration.messageType())) {
                var recipient = getRecipient(InMemoryRequestRecipient.class);
                StrongReferenceMessenger.getDefault().register(recipient, MessagePack.class, registration.channel());
            } else {
                throw new IllegalStateException("The message type is not identified as unicast or multicast type. Message type: " + registration.messageType().getName());
            }
        }
    }

    private <T extends InMemoryRecipient> T getRecipient(Class<T> type) {

        if (options.isMultipleSubscriberInstance()) {
            try {
                return provider.getServiceOrCreate(type);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create recipient instance for type: " + type, e);
            }
        } else {
            return type.cast(registrationCache.computeIfAbsent(type, k -> {
                try {
                    return provider.getServiceOrCreate(type);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create recipient instance for type: " + type, e);
                }
            }));
        }
    }
}
