package com.euonia.tuple;

import java.util.List;

/**
 * 表示包含八个值的元组，这些值可以是不同类型。
 * 此类是不可变的，提供访问值和执行常见元组操作的方法。
 *
 * @param value1 第一个值
 * @param value2 第二个值
 * @param value3 第三个值
 * @param value4 第四个值
 * @param value5 第五个值
 * @param value6 第六个值
 * @param value7 第七个值
 * @param value8 第八个值
 * @param <V1>   第一个值的类型
 * @param <V2>   第二个值的类型
 * @param <V3>   第三个值的类型
 * @param <V4>   第四个值的类型
 * @param <V5>   第五个值的类型
 * @param <V6>   第六个值的类型
 * @param <V7>   第七个值的类型
 * @param <V8>   第八个值的类型
 * @author damon(zhaorong@outlook.com)
 */
public record Octet<V1, V2, V3, V4, V5, V6, V7, V8>(V1 value1, V2 value2, V3 value3, V4 value4, V5 value5, V6 value6,
                                                    V7 value7, V8 value8) implements Tuple {
    private static final int SIZE = 8;

    /**
     * 使用指定值创建一个新的 Octet 实例。
     *
     * @param value1 第一个值
     * @param value2 第二个值
     * @param value3 第三个值
     * @param value4 第四个值
     * @param value5 第五个值
     * @param value6 第六个值
     * @param value7 第七个值
     * @param value8 第八个值
     * @param <V1>   第一个值的类型
     * @param <V2>   第二个值的类型
     * @param <V3>   第三个值的类型
     * @param <V4>   第四个值的类型
     * @param <V5>   第五个值的类型
     * @param <V6>   第六个值的类型
     * @param <V7>   第七个值的类型
     * @param <V8>   第八个值的类型
     * @return 包含指定值的新 Octet 实例
     */
    public static <V1, V2, V3, V4, V5, V6, V7, V8> Octet<V1, V2, V3, V4, V5, V6, V7, V8> of(
        final V1 value1,
        final V2 value2,
        final V3 value3,
        final V4 value4,
        final V5 value5,
        final V6 value6,
        final V7 value7,
        final V8 value8) {
        return new Octet<>(value1, value2, value3, value4, value5, value6, value7, value8);
    }

    /**
     * 从指定值数组创建一个新的 Octet 实例。
     *
     * @param values 值数组
     * @param <X>    值的类型
     * @return 一个新的 Octet 实例
     */
    public static <X> Octet<X, X, X, X, X, X, X, X> from(final X[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.length != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Octet<>(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
    }

    /**
     * 从指定值列表创建一个新的 Octet 实例。
     *
     * @param values 值列表
     * @param <X>    值的类型
     * @return 一个新的 Octet 实例
     */
    public static <X> Octet<X, X, X, X, X, X, X, X> from(final List<X> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.size() != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Octet<>(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5), values.get(6), values.get(7));
    }

    /**
     * 创建一个所有值均为 null 的新 Octet 实例。
     *
     * @param <V1> 第一个值的类型
     * @param <V2> 第二个值的类型
     * @param <V3> 第三个值的类型
     * @param <V4> 第四个值的类型
     * @param <V5> 第五个值的类型
     * @param <V6> 第六个值的类型
     * @param <V7> 第七个值的类型
     * @param <V8> 第八个值的类型
     * @return 所有值均为 null 的新 Octet 实例
     */
    public static <V1, V2, V3, V4, V5, V6, V7, V8> Octet<V1, V2, V3, V4, V5, V6, V7, V8> empty() {
        return new Octet<>(null, null, null, null, null, null, null, null);
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public List<Object> values() {
        return List.of(value1, value2, value3, value4, value5, value6, value7, value8);
    }
}
