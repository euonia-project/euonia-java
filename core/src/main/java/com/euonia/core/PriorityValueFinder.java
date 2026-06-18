package com.euonia.core;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 用于在优先级队列中根据过滤谓词查找值的工具类。
 * 该类提供方法在优先级队列中搜索并返回第一个匹配给定过滤条件的值。
 * 如果未找到匹配值，则返回指定的默认值。
 *
 * @author <a href="mailto:zhaorong@outlook.com>">damon(zhaorong@outlook.com)</a>
 */
public final class PriorityValueFinder {

    /**
     * 在优先级队列中查找第一个匹配给定过滤条件的值。
     * 如果未找到匹配值，则返回默认值。
     *
     * @param values       要搜索的优先级队列
     * @param filter       应用于每个值的过滤谓词
     * @param defaultValue 未找到匹配值时返回的默认值
     * @param <T>          优先级队列中值的类型
     * @return 第一个匹配的值，如果未找到则返回默认值
     */
    public static <T> T find(PriorityQueue<Supplier<T>, Integer> values, Predicate<T> filter, T defaultValue) {

        if (values == null) {
            throw new IllegalArgumentException("Values 队列不能为 null 或空");
        }

        if (filter == null) {
            throw new IllegalArgumentException("Filter 不能为 null");
        }

        if (values.isEmpty()) {
            return defaultValue;
        }

        while (!values.isEmpty()) {
            T value = values.poll().get();
            if (filter.test(value)) {
                return value;
            }
        }
        return defaultValue;
    }

    /**
     * 在由 consumer 提供的优先级队列中查找第一个匹配给定过滤条件的值。
     * 如果未找到匹配值，则返回默认值。
     *
     * @param queueConsumer 提供要搜索的优先级队列的 consumer
     * @param filter        应用于每个值的过滤谓词
     * @param defaultValue  未找到匹配值时返回的默认值
     * @param <T>           优先级队列中值的类型
     * @return 第一个匹配的值，如果未找到则返回默认值
     */
    public static <T> T find(Consumer<PriorityQueue<Supplier<T>, Integer>> queueConsumer, Predicate<T> filter,
            T defaultValue) {
        if (queueConsumer == null) {
            throw new IllegalArgumentException("Queue consumer 不能为 null");
        }

        var queue = new PriorityQueue<Supplier<T>, Integer>();
        queueConsumer.accept(queue);
        return find(queue, filter, defaultValue);
    }
}
