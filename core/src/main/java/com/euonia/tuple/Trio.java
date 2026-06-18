package com.euonia.tuple;

import java.io.Serial;
import java.util.List;

/**
 * 包含三个值的元组。
 *
 * @param value1 元组的第一个值
 * @param value2 元组的第二个值
 * @param value3 元组的第三个值
 * @param <T1>   第一个值的类型
 * @param <T2>   第二个值的类型
 * @param <T3>   第三个值的类型
 *
 * @author damon(zhaorong@outlook.com)
 */
public record Trio<T1, T2, T3>(T1 value1, T2 value2, T3 value3) implements Tuple {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int SIZE = 3;

    /**
     * 使用指定值创建一个新的 Trio 实例。
     *
     * @param value1 元组的第一个值
     * @param value2 元组的第二个值
     * @param value3 元组的第三个值
     * @param <T1>   第一个值的类型
     * @param <T2>   第二个值的类型
     * @param <T3>   第三个值的类型
     * @return 包含指定值的新 Trio 实例
     */
    public static <T1, T2, T3> Trio<T1, T2, T3> of(T1 value1, T2 value2, T3 value3) {
        return new Trio<>(value1, value2, value3);
    }

    /**
     * 从指定数组创建一个新的 Trio 实例。
     *
     * @param values 值数组
     * @param <X>    值的类型
     * @return 包含指定值的新 Trio 实例
     */
    public static <X> Trio<X, X, X> from(final X[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.length != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Trio<>(values[0], values[1], values[2]);
    }

    /**
     * 从指定列表创建一个新的 Trio 实例。
     *
     * @param values 值列表
     * @param <X>    值的类型
     * @return 包含指定值的新 Trio 实例
     */
    public static <X> Trio<X, X, X> from(final List<X> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.size() != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Trio<>(values.get(0), values.get(1), values.get(2));
    }

    /**
     * 创建一个所有值均为 null 的新 Trio 实例。
     *
     * @param <T1> 第一个值的类型
     * @param <T2> 第二个值的类型
     * @param <T3> 第三个值的类型
     * @return 所有值均为 null 的新 Trio 实例
     */
    public static <T1, T2, T3> Trio<T1, T2, T3> empty() {
        return new Trio<>(null, null, null);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public List<Object> values() {
        return List.of(value1, value2, value3);
    }
}
