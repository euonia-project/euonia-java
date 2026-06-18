package com.euonia.tuple;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 表示一个有序的值集合，其中每个值可以是任意类型。元组是不可变的，可以包含重复值。
 * 提供了访问值、检查值是否存在以及执行常见元组操作（如相等性检查、转换为列表和数组）的方法。
 * Tuple 接口继承 Iterable&lt;Object&gt; 以便于遍历元组中的值，同时还实现了 Serializable 以支持元组实例的序列化。
 * Tuple 接口设计灵活，可由表示不同大小和类型的元组的各种类来实现（如 Pair、Triplet、Quadruple 等）。
 * 它为操作元组提供了通用约定，允许在不同的实现和用例中一致地处理元组。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Tuple extends Iterable<Object>, Serializable {
    /**
     * 返回元组中值的数量。
     *
     * @return 元组中值的数量
     */
    int size();

    /**
     * 返回元组中指定索引位置的值。
     *
     * @param index 要返回的值的索引
     * @return 指定索引位置的值
     * @throws IndexOutOfBoundsException 如果索引超出范围
     */
    default Object value(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("索引：" + index + "，大小：" + size());
        }
        return values().get(index);
    }

    /**
     * 返回包含元组中所有值的列表。
     *
     * @return 包含元组中所有值的列表
     */
    List<Object> values();

    /**
     * 检查元组是否包含指定的值。
     *
     * @param value 要检查的值
     * @return 如果元组包含该值则返回 true，否则返回 false
     */
    default boolean contains(final Object value) {
        return values().stream().anyMatch(v -> Objects.equals(v, value));
    }

    /**
     * 检查元组是否包含所有指定的值。
     *
     * @param values 要检查的值
     * @return 如果元组包含所有值则返回 true，否则返回 false
     */
    default boolean containsAll(final Collection<?> values) {
        for (final Object value : values) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查元组是否包含指定元组中的所有值。
     *
     * @param tuple 要检查其值的元组
     * @return 如果元组包含指定元组中的所有值则返回 true，否则返回 false
     */
    default boolean containsAll(final Tuple tuple) {
        return containsAll(tuple.values());
    }

    /**
     * 检查元组是否包含所有指定的值。
     *
     * @param values 要检查的值
     * @return 如果元组包含所有值则返回 true，否则返回 false
     */
    default boolean containsAll(final Object... values) {
        for (final Object value : values) {
            if (!contains(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查元组是否包含任意一个指定的值。
     *
     * @param values 要检查的值
     * @return 如果元组包含任意一个值则返回 true，否则返回 false
     */
    default boolean containsAny(final Collection<?> values) {
        for (final Object value : values) {
            if (contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查元组是否包含指定元组中的任意一个值。
     *
     * @param tuple 要检查其值的元组
     * @return 如果元组包含指定元组中的任意一个值则返回 true，否则返回 false
     */
    default boolean containsAny(final Tuple tuple) {
        return containsAny(tuple.values());
    }

    /**
     * 检查元组是否包含任意一个指定的值。
     *
     * @param values 要检查的值
     * @return 如果元组包含任意一个值则返回 true，否则返回 false
     */
    default boolean containsAny(final Object... values) {
        for (final Object value : values) {
            if (contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回指定值在元组中首次出现的索引，如果未找到则返回 -1。
     *
     * @param value 要搜索的值
     * @return 指定值首次出现的索引，如果未找到则返回 -1
     */
    default int indexOf(final Object value) {
        final List<?> values = values();
        final int size = this.size();
        for (int i = 0; i < size; i++) {
            if (Objects.equals(values.get(i), value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 返回指定值在元组中最后一次出现的索引，如果未找到则返回 -1。
     *
     * @param value 要搜索的值
     * @return 指定值最后一次出现的索引，如果未找到则返回 -1
     */
    default int lastIndexOf(final Object value) {
        final List<?> values = values();
        for (int i = this.size() - 1; i >= 0; i--) {
            if (Objects.equals(values.get(i), value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 返回包含元组中所有值的列表。返回的列表是不可变的，反映元组中值的顺序。
     *
     * @return 包含元组中所有值的列表
     */
    default List<Object> toList() {
        return this.values(); // 已经返回不可变列表
    }

    /**
     * 返回包含元组中所有值的数组。返回的数组是一个新数组，反映元组中值的顺序。
     *
     * @return 包含元组中所有值的数组
     */
    default Object[] toArray() {
        return this.values().toArray();
    }

    /**
     * 返回包含元组中所有值的数组。如果提供的数组足够大以容纳所有值，则将使用它；否则将分配一个相同运行时类型的新数组。
     *
     * @param array 用于存储元组元素的数组（如果足够大）；否则将分配一个相同运行时类型的新数组
     * @param <X>   用于包含元组值的数组的组件类型
     * @return 包含元组中所有值的数组
     */
    default <X> X[] toArray(final X[] array) {
        return this.values().toArray(array);
    }

    /**
     * 返回元组中值的迭代器。返回的迭代器是不可变的，反映元组中值的顺序。
     *
     * @return 元组中值的迭代器
     */
    @SuppressWarnings("NullableProblems")
    @Override
    default Iterator<Object> iterator() {
        return values().iterator();
    }

    /**
     * 检查此元组是否等于另一个元组，忽略值的顺序。如果两个元组包含相同的值且出现次数相同，则认为它们相等，不考虑顺序。
     *
     * @param other 要比较的另一个元组
     * @return 如果忽略顺序后元组相等则返回 true，否则返回 false
     */
    default boolean equalsIgnoreOrder(final Tuple other) {

        if (this == other) {
            return true;
        }
        final int size = this.size();
        if (other == null || size != other.size()) {
            return false;
        }

        // 存储每个元素及其出现次数，然后使用另一个 Map 来减少
        final Map<Object, Integer> valueOccurrences = new HashMap<>(size + 1, 1.0f);

        for (int i = 0; i < size; i++) {
            final Object thisValue = this.value(i);
            valueOccurrences.merge(thisValue, 1, Integer::sum);
        }

        for (int i = 0; i < size; i++) {
            final Object otherValue = other.value(i);
            int occ = valueOccurrences.merge(otherValue, -1, Integer::sum);
            if (occ < 0) {
                return false; // 元素不存在：元组不同
            } else if (occ == 0) {
                valueOccurrences.remove(otherValue);
            }
        }

        return valueOccurrences.isEmpty();
    }
}
