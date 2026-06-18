package com.euonia.reflection;

/**
 * AmbiguousMethodException 是一个自定义异常，当多个方法匹配工厂方法的条件时抛出，
 * 导致无法明确应使用哪个方法。
 * 它继承自 RuntimeException，并提供描述方法选择过程中歧义的消息。
 * 此异常通常在存在多个同名或同注解的方法都可能是工厂方法，
 * 且提供的条件无法清晰区分它们时抛出。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class AmbiguousMethodException extends RuntimeException {
    /**
     * 使用指定的消息构造一个新的 AmbiguousMethodException。
     *
     * @param message 描述方法选择过程中歧义的消息。
     */
    public AmbiguousMethodException(String message) {
        super(message);
    }

    /**
     * 使用指定的消息和原因构造一个新的 AmbiguousMethodException。
     *
     * @param message 描述方法选择过程中歧义的消息。
     * @param cause   导致此异常的原因。
     */
    public AmbiguousMethodException(String message, Throwable cause) {
        super(message, cause);
    }
}
