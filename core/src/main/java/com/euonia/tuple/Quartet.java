package com.euonia.tuple;

import java.io.Serial;
import java.util.List;

/**
 * 表示包含四个值的元组，这些值可以是不同类型。
 * 此类是不可变的，提供访问值和执行常见元组操作的方法。
 *
 * @param value1 元组的第一个值
 * @param value2 元组的第二个值
 * @param value3 元组的第三个值
 * @param value4 元组的第四个值
 * @param <V1>   第一个值的类型
 * @param <V2>   第二个值的类型
 * @param <V3>   第三个值的类型
 * @param <V4>   第四个值的类型
 * @author damon(zhaorong@outlook.com)
 */
public record Quartet<V1, V2, V3, V4>(V1 value1, V2 value2, V3 value3, V4 value4) implements Tuple {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int SIZE = 4;

    /**
     * 使用指定值创建一个新的 Quartet 实例。
     *
     * @param value1 元组的第一个值
     * @param value2 元组的第二个值
     * @param value3 元组的第三个值
     * @param value4 元组的第四个值
     * @param <V1>   第一个值的类型
     * @param <V2>   第二个值的类型
     * @param <V3>   第三个值的类型
     * @param <V4>   第四个值的类型
     * @return 包含指定值的新 Quartet 实例
     */
    public static <V1, V2, V3, V4> Quartet<V1, V2, V3, V4> of(V1 value1, V2 value2, V3 value3, V4 value4) {
        return new Quartet<>(value1, value2, value3, value4);
    }

    /**
     * 从指定数组创建一个新的 Quartet 实例。
     *
     * @param values 值数组
     * @param <X>    值的类型
     * @return 包含指定值的新 Quartet 实例
     */
    public static <X> Quartet<X, X, X, X> from(final X[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.length != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Quartet<>(values[0], values[1], values[2], values[3]);
    }

    /**
     * 从指定列表创建一个新的 Quartet 实例。
     *
     * @param values 值列表
     * @param <X>    值的类型
     * @return 包含指定值的新 Quartet 实例
     */
    public static <X> Quartet<X, X, X, X> from(final List<X> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.size() != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Quartet<>(values.get(0), values.get(1), values.get(2), values.get(3));
    }

    /**
     * 创建一个所有值均为 null 的空 Quartet 实例。
     *
     * @param <V1> 第一个值的类型
     * @param <V2> 第二个值的类型
     * @param <V3> 第三个值的类型
     * @param <V4> 第四个值的类型
     * @return 所有值均为 null 的新 Quartet 实例
     */
    public static <V1, V2, V3, V4> Quartet<V1, V2, V3, V4> empty() {
        return new Quartet<>(null, null, null, null);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public List<Object> values() {
        return List.of(value1, value2, value3, value4);
    }
}
