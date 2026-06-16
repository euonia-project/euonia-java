package com.euonia.sample;

import com.euonia.bus.*;
import com.euonia.bus.contract.Transport;
import com.euonia.bus.convention.AnnotationMessageConvention;
import com.euonia.bus.convention.DefaultMessageConvention;
import com.euonia.bus.recipient.RecipientRegistrar;
import com.euonia.bus.strategy.AnnotationTransportStrategy;
import com.euonia.reflection.ServiceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageBusConfiguration {
    @Value("${euonia.bus.default-transport:InMemoryMessageBusTransport}")
    private String defaultTransport;

    @Value("${euonia.bus.lazy-initialize:true}")
    private boolean lazyInitialize;

    @Value("${euonia.bus.inmemory.name:InMemoryMessageBusTransport}")
    private String inMemoryName;

    @Bean
    public Configurator configurator() {

        var packageName = this.getClass().getPackageName();

        return new DefaultConfigurator()
            .setConvention(c -> {
                c.add(DefaultMessageConvention.class);
                c.add(AnnotationMessageConvention.class);
                c.evaluateUnicast(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Command"));
                c.evaluateMulticast(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Event"));
                c.evaluateRequest(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Query"));
            })
            .setStrategy("InMemoryMessageBusTransport", s -> {
                s.add(new AnnotationTransportStrategy("InMemoryMessageBusTransport"));
                s.evaluateOutgoing(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Command"));
                s.evaluateIncoming(t -> t.getPackageName().startsWith(packageName) && t.getSimpleName().endsWith("Command"));
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

    private InMemoryBusOptions getInMemoryBusOptions() {
        var options = new InMemoryBusOptions();
        options.setName(inMemoryName);
        options.setLazyInitialize(lazyInitialize);
        return options;
    }
}
