package com.euonia.tuple;

import java.io.Serial;
import java.util.List;

/**
 * 表示包含两个值的元组，这两个值可以是不同类型。
 * 此类是不可变的，提供访问值和执行常见元组操作的方法。
 *
 * @param value1 第一个值
 * @param value2 第二个值
 * @param <V1>   第一个值的类型
 * @param <V2>   第二个值的类型
 * @author damon(zhaorong@outlook.com)
 */
public record Duet<V1, V2>(V1 value1, V2 value2) implements Tuple {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int SIZE = 2;

    /**
     * 使用给定值创建一个新的 {@code Duet} 实例。
     *
     * @param value1 第一个值
     * @param value2 第二个值
     * @param <V1>   第一个值的类型
     * @param <V2>   第二个值的类型
     * @return 包含给定值的新 {@code Duet} 实例
     */
    public static <V1, V2> Duet<V1, V2> of(V1 value1, V2 value2) {
        return new Duet<>(value1, value2);
    }

    /**
     * 从给定值创建一个新的 {@code Duet} 实例。
     *
     * @param values 值数组
     * @param <X>    值的类型
     * @return 包含给定值的新 {@code Duet} 实例
     */
    public static <X> Duet<X, X> from(final X[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.length != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Duet<>(values[0], values[1]);
    }

    /**
     * 从给定值创建一个新的 {@code Duet} 实例。
     *
     * @param values 值列表
     * @param <X>    值的类型
     * @return 包含给定值的新 {@code Duet} 实例
     */
    public static <X> Duet<X, X> from(final List<X> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.size() != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Duet<>(values.get(0), values.get(1));
    }

    /**
     * 创建一个所有值均为 null 的新 {@code Duet} 实例。
     *
     * @param <V1> 第一个值的类型
     * @param <V2> 第二个值的类型
     * @return 所有值均为 null 的新 {@code Duet} 实例
     */
    public static <V1, V2> Duet<V1, V2> empty() {
        return new Duet<>(null, null);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public List<Object> values() {
        return List.of(value1, value2);
    }
}
