package com.euonia.bus;

import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.reflection.ServiceProvider;
import com.euonia.utility.Assert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

@Configuration
public class MessageBusConfiguration {

    @Bean
    public Bus bus(ServiceProvider provider, Configurator configurator) {
        Assert.notNull(provider, "ServiceProvider cannot be null");
        Assert.notNull(configurator, "Configurator cannot be null");
        if (!CollectionUtils.isEmpty(configurator.getRegistrations())) {
            var registrars = provider.getServices(RecipientRegistrar.class);
            for (var registrar : registrars) {
                registrar.register(configurator.getRegistrations(), configurator.getDefaultTransport());
            }
        }
        return new MessageBus(provider, configurator);
    }

    @Bean
    public HandlerContext handlerContext(ServiceProvider provider, Configurator configurator) {

        Assert.notNull(provider, "ServiceProvider cannot be null");
        Assert.notNull(configurator, "Configurator cannot be null");
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
