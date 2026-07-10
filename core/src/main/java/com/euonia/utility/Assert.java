package com.euonia.utility;

import java.util.List;
import java.util.function.Predicate;

/**
 * 参数断言工具类，提供常用的前置条件检查方法。
 * 当条件不满足时，抛出 {@link IllegalArgumentException}。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class Assert {
    /**
     * 断言对象不为 null。
     *
     * @param obj     要检查的对象
     * @param message 断言失败时的错误消息
     * @throws IllegalArgumentException 如果对象为 null
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象不为 null，使用 Supplier 延迟生成错误消息。
     *
     * @param obj             要检查的对象
     * @param messageSupplier 断言失败时的错误消息提供者
     * @throws IllegalArgumentException 如果对象为 null
     */
    public static void notNull(Object obj, java.util.function.Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    /**
     * 断言字符串不为空或空白。
     *
     * @param str     要检查的字符串
     * @param message 断言失败时的错误消息
     * @throws IllegalArgumentException 如果字符串为 null、空或空白
     */
    public static void notEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty() || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言列表不为空。
     *
     * @param list    要检查的列表
     * @param message 断言失败时的错误消息
     * @throws IllegalArgumentException 如果列表为 null 或空
     */
    public static void notEmpty(List<?> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数组不为空。
     *
     * @param array   要检查的数组
     * @param message 断言失败时的错误消息
     * @throws IllegalArgumentException 如果数组为 null 或空
     */
    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言列表中不包含与给定断言匹配的任何元素。
     *
     * @param <T>       列表元素类型
     * @param list      要检查的列表
     * @param predicate 匹配断言
     * @param message   断言失败时的错误消息
     * @throws IllegalArgumentException 如果列表包含匹配断言元素
     */
    public static <T> void notContains(List<T> list, Predicate<T> predicate, String message) {
        if (list != null && list.stream().anyMatch(predicate)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数组中不包含与给定断言匹配的任何元素。
     *
     * @param <T>       数组元素类型
     * @param array     要检查的数组
     * @param predicate 匹配断言
     * @param message   断言失败时的错误消息
     * @throws IllegalArgumentException 如果数组包含匹配断言元素
     */
    public static <T> void notContains(T[] array, Predicate<T> predicate, String message) {
        if (array != null && java.util.Arrays.stream(array).anyMatch(predicate)) {
            throw new IllegalArgumentException(message);
        }
    }
}
