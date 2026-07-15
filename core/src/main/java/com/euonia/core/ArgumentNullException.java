package com.euonia.core;

import com.euonia.utility.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ArgumentNullException 是一个自定义异常类，用于表示方法参数为 null 的情况。
 * 当方法参数为 null 时，可以抛出此异常，以便调用者能够明确地知道参数不应为 null。
 * <p>
 * 该异常继承自 IllegalArgumentException，因此可以在需要时捕获此异常类型。
 * </p>
 * <p>
 * 提供了多个静态方法，用于检查参数是否为 null 或空，并在不满足条件时抛出 ArgumentNullException。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * ArgumentNullException.throwIfNull(argument, "argumentName");
 * ArgumentNullException.throwIfNullOrEmpty(stringArgument, "stringArgumentName");
 * </pre>
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ArgumentNullException extends IllegalArgumentException {

    /**
     * 构造一个新的 ArgumentNullException 异常，带有指定的详细消息。
     *
     * @param message 异常的详细消息
     */
    public ArgumentNullException(String message) {
        super(message);
    }

    /**
     * 检查参数是否为 null，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的参数
     * @param param    参数名称
     */
    public static void throwIfNull(Object argument, String param) {
        throwIfNull(argument, () -> Resource.getString("core", "ArgumentNullException.Message1", param));
    }

    /**
     * 检查字符串是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的字符串
     * @param param    参数名称
     */
    public static void throwIfNullOrEmpty(String argument, String param) {
        throwIfNullOrEmpty(argument, () -> Resource.getString("core", "ArgumentNullException.Message2", param));
    }

    /**
     * 检查数组是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的数组
     * @param param    参数名称
     * @param <T>      数组中元素的类型
     */
    public static <T> void throwIfNullOrEmpty(T[] argument, String param) {
        throwIfNullOrEmpty(argument, () -> Resource.getString("core", "ArgumentNullException.Message2", param));
    }

    /**
     * 检查 List 是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的 List
     * @param param    参数名称
     * @param <T>      List 中元素的类型
     */
    public static <T> void throwIfNullOrEmpty(List<T> argument, String param) {
        throwIfNullOrEmpty(argument, () -> Resource.getString("core", "ArgumentNullException.Message2", param));
    }

    /**
     * 检查集合是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的集合
     * @param param    参数名称
     * @param <T>      集合中元素的类型
     */
    public static <T> void throwIfNullOrEmpty(Collection<T> argument, String param) {
        throwIfNullOrEmpty(argument, () -> Resource.getString("core", "ArgumentNullException.Message2", param));
    }

    /**
     * 检查 Map 是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的 Map
     * @param param    参数名称
     * @param <K>      Map 的键类型
     * @param <V>      Map 的值类型
     */
    public static <K, V> void throwIfNullOrEmpty(Map<K, V> argument, String param) {
        throwIfNullOrEmpty(argument, () -> Resource.getString("core", "ArgumentNullException.Message2", param));
    }

    /**
     * 检查参数是否为 null，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的参数
     * @param message  异常消息的提供者
     */
    public static void throwIfNull(Object argument, Supplier<String> message) {
        if (argument == null) {
            throw new ArgumentNullException(message.get());
        }
    }

    /**
     * 检查字符串是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的字符串
     * @param message  异常消息的提供者
     */
    public static void throwIfNullOrEmpty(String argument, Supplier<String> message) {
        if (argument == null || argument.isEmpty()) {
            throw new ArgumentNullException(message.get());
        }
    }

    /**
     * 检查数组是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的数组
     * @param message  异常消息的提供者
     * @param <T>      数组中元素的类型
     */
    public static <T> void throwIfNullOrEmpty(T[] argument, Supplier<String> message) {
        if (argument == null || argument.length == 0) {
            throw new ArgumentNullException(message.get());
        }
    }

    /**
     * 检查 List 是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的 List
     * @param message  异常消息的提供者
     * @param <T>      List 中元素的类型
     */
    public static <T> void throwIfNullOrEmpty(List<T> argument, Supplier<String> message) {
        if (argument == null || argument.isEmpty()) {
            throw new ArgumentNullException(message.get());
        }
    }

    /**
     * 检查 Map 是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的 Map
     * @param message  异常消息的提供者
     * @param <K>      Map 的键类型
     * @param <V>      Map 的值类型
     */
    public static <K, V> void throwIfNullOrEmpty(Map<K, V> argument, Supplier<String> message) {
        if (argument == null || argument.isEmpty()) {
            throw new ArgumentNullException(message.get());
        }
    }

    /**
     * 检查集合是否为 null 或空，如果是，则抛出 ArgumentNullException 异常。
     *
     * @param argument 要检查的集合
     * @param message  异常消息的提供者
     * @param <T>      集合中元素的类型
     */
    public static <T> void throwIfNullOrEmpty(Collection<T> argument, Supplier<String> message) {
        if (argument == null || argument.isEmpty()) {
            throw new ArgumentNullException(message.get());
        }
    }
}
