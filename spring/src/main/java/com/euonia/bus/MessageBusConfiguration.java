package com.euonia.bus;

import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.reflection.ServiceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;

@Configuration
public class MessageBusConfiguration {

    @Bean
    public Bus bus(ServiceProvider provider, Configurator configurator) {
        if (configurator != null && configurator.getRegistrations() != null) {
            var registrars = provider.getServices(RecipientRegistrar.class);
            for (var registrar : registrars) {
                registrar.register(configurator.getRegistrations(), configurator.getDefaultTransport());
            }
        }
        var dispatcher = new StrategicDispatcher(configurator);
        return new MessageBus(provider, dispatcher, configurator);
    }

    @Bean
    public HandlerContext handlerContext(ServiceProvider provider) {

        var context = new DefaultHandlerContext(provider);

        var configurator = provider.getService(Configurator.class)
                                   .orElse(null);

        try {
            var genericRegisterMethod = DefaultHandlerContext.class.getDeclaredMethod("register", Class.class, Class.class);
            if (configurator != null) {
                for (var registration : configurator.getRegistrations()) {
                    if (registration.method().getAnnotation(Override.class) != null) {
                        genericRegisterMethod.invoke(context, registration.messageType(), registration.handlerType());
                    } else {
                        context.register(registration);
                    }
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return context;
    }
}
