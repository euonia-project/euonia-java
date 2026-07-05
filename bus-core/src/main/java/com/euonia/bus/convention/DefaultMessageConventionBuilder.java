package com.euonia.bus.convention;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Predicate;

import com.euonia.utility.Assert;

/**
 * Default implementation of the {@link MessageConventionBuilder} interface.
 * It provides methods to define message conventions for unicast, multicast, and request messages.
 */
public class DefaultMessageConventionBuilder implements MessageConventionBuilder {
    private final BaseMessageConvention convention = new BaseMessageConvention();

    /**
     * Gets the MessageConvention instance that has been built by this builder.
     *
     * @return the built MessageConvention instance
     */
    @Override
    public MessageConvention getConvention() {
        return this.convention;
    }

    /**
     * Adds a message convention that will be used to evaluate whether a type is a unicast message.
     *
     * @param predicate the predicate to evaluate the unicast message type
     * @return the current instance of DefaultMessageConventionBuilder
     */
    @Override
    public MessageConventionBuilder evaluateUnicast(Predicate<String> predicate) {
        Assert.notNull(predicate, "predicate cannot be null.");
        this.convention.defineUnicastTypeConvention(predicate);
        return this;
    }

    /**
     * Adds a message convention that will be used to evaluate whether a type is a multicast message.
     *
     * @param predicate the predicate to evaluate the multicast message type
     * @return the current instance of MessageConventionBuilder
     */
    @Override
    public MessageConventionBuilder evaluateMulticast(Predicate<String> predicate) {
        Assert.notNull(predicate, "predicate cannot be null.");
        this.convention.defineMulticastTypeConvention(predicate);
        return this;
    }

    /**
     * Adds a message convention that will be used to evaluate whether a type is a request message.
     *
     * @param predicate the predicate to evaluate the request message type
     * @return the current instance of MessageConventionBuilder
     */
    @Override
    public MessageConventionBuilder evaluateRequest(Predicate<String> predicate) {
        Assert.notNull(predicate, "predicate cannot be null.");
        this.convention.defineRequestTypeConvention(predicate);
        return this;
    }

    /**
     * Adds a message convention that will be used to evaluate whether a type is a unicast, multicast, or request message.
     *
     * @param convention the function to evaluate the message type
     * @return the current instance of MessageConventionBuilder
     */
    @Override
    public MessageConventionBuilder evaluate(Function<String, MessageConventionType> convention) {
        Assert.notNull(convention, "convention cannot be null.");
        this.convention.defineTypeConvention(convention);
        return this;
    }

    /**
     * Adds a message convention that will be used to evaluate whether a type is a unicast, multicast, or request message.
     *
     * @param convention the MessageConvention instance to add
     * @param <C>        the type of the MessageConvention
     * @return the current instance of MessageConventionBuilder
     */
    @Override
    public <C extends MessageConvention> MessageConventionBuilder add(C convention) {
        Assert.notNull(convention, "convention cannot be null.");
        this.convention.add(convention);
        return this;
    }

    /**
     * Adds a message convention that will be used to evaluate whether a type is a unicast, multicast, or request message.
     * The convention will be instantiated using its default constructor.
     *
     * @param conventionClass the class of the MessageConvention to add
     * @param <C>             the type of the MessageConvention
     * @return the current instance of MessageConventionBuilder
     */
    @Override
    public <C extends MessageConvention> MessageConventionBuilder add(Class<C> conventionClass) {
        Assert.notNull(conventionClass, "conventionClass cannot be null.");
        try {
            C instance = conventionClass.getDeclaredConstructor().newInstance();
            this.convention.add(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to instantiate convention: " + conventionClass, exception);
        }
        return this;
    }
}
