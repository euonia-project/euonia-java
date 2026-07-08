package com.euonia.bus.convention;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * {@link MessageConventionBuilder} 提供了配置消息约定的流式 API，允许用户通过断言或自定义约定实现来定义消息类型（单播、多播、请求）的分类规则。
 * <p>
 * 此构建器允许用户轻松地自定义消息分类逻辑，提供使用断言来评估单播、多播和请求类型的方法，以及使用自定义函数评估所有消息类型的方法。此外，用户还可以直接将自定义的 {@link MessageConvention} 实现添加到构建器中。
 * </p>
 * <p>
 * 构建完成的 {@link MessageConvention} 可通过 {@link #getConvention()} 方法检索，该方法返回一个聚合了所有已定义约定并在对消息类型进行分类时应用它们的 {@link BaseMessageConvention} 实例。此构建器简化了配置消息约定的过程，并为总线系统中的消息分类提供了灵活且可扩展的方法。
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface MessageConventionBuilder {

    /**
     * 获取此构建器已构建的 {@link MessageConvention} 实例。
     *
     * @return 已构建的 MessageConvention 实例
     */
    MessageConvention getConvention();

    /**
     * 添加一个消息约定，用于评估给定通道是否为单播消息。
     *
     * @param predicate 用于评估单播消息类型的断言
     * @return 当前 MessageConventionBuilder 实例
     */
    MessageConventionBuilder evaluateUnicast(Predicate<String> predicate);


    /**
     * 添加一个消息约定，用于评估给定通道是否为多播消息。
     *
     * @param predicate 用于评估多播消息类型的断言
     * @return 当前 MessageConventionBuilder 实例
     */
    MessageConventionBuilder evaluateMulticast(Predicate<String> predicate);

    /**
     * 添加一个消息约定，用于评估给定通道是否为请求消息。
     *
     * @param predicate 用于评估请求消息类型的断言
     * @return 当前 MessageConventionBuilder 实例
     */
    MessageConventionBuilder evaluateRequest(Predicate<String> predicate);

    /**
     * 添加一个消息约定，使用函数评估给定通道是哪种类型。
     *
     * @param convention 用于评估消息类型的函数
     * @return 当前 MessageConventionBuilder 实例
     */
    MessageConventionBuilder evaluate(Function<String, MessageConventionType> convention);

    /**
     * 添加一个消息约定实例。
     *
     * @param <C>        MessageConvention 的类型
     * @param convention 要添加的 MessageConvention 实例
     * @return 当前 MessageConventionBuilder 实例
     */
    <C extends MessageConvention> MessageConventionBuilder add(C convention);

    /**
     * 添加一个消息约定类，该约定将使用默认构造函数实例化。
     *
     * @param <C>             MessageConvention 的类型
     * @param conventionClass 要添加的 MessageConvention 类
     * @return 当前 MessageConventionBuilder 实例
     */
    <C extends MessageConvention> MessageConventionBuilder add(Class<C> conventionClass);
}
