package com.euonia.bus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

import com.euonia.core.ArgumentNullException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.reflection.ServiceProvider;

/**
 * 消息总线的 Spring 配置类，负责创建和注册消息总线相关的 Bean。
 * <p>
 * 配置以下 Bean：
 * <ul>
 *   <li>{@link Bus} —— 消息总线实例，用于发布/发送/调用消息</li>
 *   <li>{@link HandlerContext} —— 处理器上下文，用于注册和管理消息处理器</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Configuration
public class MessageBusConfiguration {

    /**
     * 创建并配置 {@link Bus} Bean。
     * 如果配置器中有注册的处理器，会先通过 {@link RecipientRegistrar} 进行注册。
     *
     * @param provider     服务提供者
     * @param configurator 消息总线配置器
     * @return 配置好的消息总线实例
     */
    @Bean
    public Bus bus(ServiceProvider provider, Configurator configurator) {
        ArgumentNullException.throwIfNull(provider, "ServiceProvider");
        ArgumentNullException.throwIfNull(configurator, "Configurator");
        if (!CollectionUtils.isEmpty(configurator.getRegistrations())) {
            var registrars = provider.getServices(RecipientRegistrar.class);
            for (var registrar : registrars) {
                registrar.register(configurator.getRegistrations(), configurator.getDefaultTransport());
            }
        }
        return new MessageBus(provider, configurator);
    }

    /**
     * 创建并配置 {@link HandlerContext} Bean。
     * <p>
     * 遍历配置器中的所有处理器注册，依据处理器是否实现 {@link Handler} 接口
     * 来选择合适的注册方式。实现接口的处理器使用泛型注册方法，
     * 注解方式的处理器使用直接注册。
     *
     * @param provider     服务提供者
     * @param configurator 消息总线配置器
     * @return 配置好的处理器上下文实例
     */
    @Bean
    public HandlerContext handlerContext(ServiceProvider provider, Configurator configurator) {

        ArgumentNullException.throwIfNull(provider, "ServiceProvider");
        ArgumentNullException.throwIfNull(configurator, "Configurator");

        var context = new DefaultHandlerContext(provider);

        try {
            var genericRegisterMethod = DefaultHandlerContext.class.getDeclaredMethod("register", String.class, Class.class, Class.class);

            for (var entry : configurator.getRegistrations().entrySet()) {

                String channel = entry.getKey();
                ChannelRegistration registration = entry.getValue();

                for (var handler : registration.getHandlers()) {
                    var isImplementHandlerInterface = Arrays.stream(handler.handlerType().getGenericInterfaces()).anyMatch(type -> {
                        if (!(type instanceof ParameterizedType parameterizedType) || parameterizedType.getActualTypeArguments().length != 2) {
                            return false;
                        }
                        if (parameterizedType.getRawType() != Handler.class) {
                            return false;
                        }
                        if (parameterizedType.getActualTypeArguments()[0] != registration.getMessageType()) {
                            return false;
                        }
                        if (parameterizedType.getActualTypeArguments()[1] != MessageContext.class) {
                            return false;
                        }

                        {
                        }

                        return true;
                    });
                    if (isImplementHandlerInterface) {
                        genericRegisterMethod.invoke(context, channel, registration.getMessageType(), handler.handlerType());
                    } else {
                        context.register(channel, handler);
                    }
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return context;
    }
}
