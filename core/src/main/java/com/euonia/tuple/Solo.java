package com.euonia.tuple;

import java.io.Serial;
import java.util.List;

/**
 * 表示包含单个值的元组，该值可以是任意类型。
 * 此类是不可变的，提供访问值和执行常见元组操作的方法。
 *
 * @param value 元组的值
 * @param <V>   值的类型
 * @author damon(zhaorong@outlook.com)
 */
public record Solo<V>(V value) implements Tuple {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int SIZE = 1;

    /**
     * 使用指定值创建一个新的 Solo 实例。
     *
     * @param value 元组的值
     * @param <V>   值的类型
     * @return 包含指定值的新 Solo 实例
     */
    public static <V> Solo<V> of(V value) {
        return new Solo<>(value);
    }

    /**
     * 从指定数组创建一个新的 Solo 实例。
     *
     * @param values 值数组
     * @param <X>    值的类型
     * @return 包含指定值的新 Solo 实例
     */
    public static <X> Solo<X> from(final X[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.length != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Solo<>(values[0]);
    }

    /**
     * 从指定列表创建一个新的 Solo 实例。
     *
     * @param values 值列表
     * @param <X>    值的类型
     * @return 包含指定值的新 Solo 实例
     */
    public static <X> Solo<X> from(final List<X> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values 不能为空");
        }
        if (values.size() != SIZE) {
            throw new IllegalArgumentException("values 必须具有相同的大小");
        }
        return new Solo<>(values.get(0));
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public java.util.List<Object> values() {
        return java.util.Collections.singletonList(value);
    }

    @Override
    public boolean contains(Object value) {
        return java.util.Objects.equals(this.value, value);
    }
}
