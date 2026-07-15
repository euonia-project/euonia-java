package com.euonia.bus.convention;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Predicate;

import com.euonia.core.ArgumentNullException;
import com.euonia.utility.Assert;
import com.euonia.utility.Resource;

/**
 * {@link MessageConventionBuilder} 接口的默认实现。
 * 提供了定义单播、多播和请求消息的消息约定的方法。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultMessageConventionBuilder implements MessageConventionBuilder {
    private final BaseMessageConvention convention = new BaseMessageConvention();

    /**
     * 获取此构建器已构建的 {@link MessageConvention} 实例。
     *
     * @return 已构建的 MessageConvention 实例
     */
    @Override
    public MessageConvention getConvention() {
        return this.convention;
    }

    /**
     * 添加一个消息约定，用于评估给定通道是否为单播消息。
     *
     * @param predicate 用于评估单播消息类型的断言
     * @return 当前 DefaultMessageConventionBuilder 实例
     */
    @Override
    public MessageConventionBuilder evaluateUnicast(Predicate<String> predicate) {
        Assert.notNull(predicate, "predicate cannot be null.");
        this.convention.defineUnicastTypeConvention(predicate);
        return this;
    }

    /**
     * 添加一个消息约定，用于评估给定通道是否为多播消息。
     *
     * @param predicate 用于评估多播消息类型的断言
     * @return 当前 MessageConventionBuilder 实例
     */
    @Override
    public MessageConventionBuilder evaluateMulticast(Predicate<String> predicate) {
        ArgumentNullException.throwIfNull(predicate, "predicate");
        this.convention.defineMulticastTypeConvention(predicate);
        return this;
    }

    /**
     * 添加一个消息约定，用于评估给定通道是否为请求消息。
     *
     * @param predicate 用于评估请求消息类型的断言
     * @return 当前 MessageConventionBuilder 实例
     */
    @Override
    public MessageConventionBuilder evaluateRequest(Predicate<String> predicate) {
        ArgumentNullException.throwIfNull(predicate, "predicate");
        this.convention.defineRequestTypeConvention(predicate);
        return this;
    }

    /**
     * 添加一个消息约定，使用函数评估给定通道是哪种消息类型。
     *
     * @param convention 用于评估消息类型的函数
     * @return 当前 MessageConventionBuilder 实例
     */
    @Override
    public MessageConventionBuilder evaluate(Function<String, MessageConventionType> convention) {
        ArgumentNullException.throwIfNull(convention, "convention");
        this.convention.defineTypeConvention(convention);
        return this;
    }

    /**
     * 添加一个消息约定实例。
     *
     * @param <C>        MessageConvention 的类型
     * @param convention 要添加的 MessageConvention 实例
     * @return 当前 MessageConventionBuilder 实例
     */
    @Override
    public <C extends MessageConvention> MessageConventionBuilder add(C convention) {
        ArgumentNullException.throwIfNull(convention, "convention");
        this.convention.add(convention);
        return this;
    }

    /**
     * 添加一个消息约定类，该约定将使用默认构造函数实例化。
     *
     * @param <C>             MessageConvention 的类型
     * @param conventionClass 要添加的 MessageConvention 类
     * @return 当前 MessageConventionBuilder 实例
     */
    @Override
    public <C extends MessageConvention> MessageConventionBuilder add(Class<C> conventionClass) {
        ArgumentNullException.throwIfNull(conventionClass, "conventionClass");
        try {
            C instance = conventionClass.getDeclaredConstructor().newInstance();
            this.convention.add(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException exception) {
            throw new RuntimeException(Resource.getString("resource", "DefaultMessageConventionBuilder.FailedToInstantiateConvention", conventionClass.getName()), exception);
        }
        return this;
    }
}
