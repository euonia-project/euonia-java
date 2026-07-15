package com.euonia.core;

/**
 * 表示一个范围的泛型类，定义了最小值和最大值。类型 T 必须实现 Comparable 接口，以便可以比较范围的边界。
 *
 * @param min 范围的最小值
 * @param max 范围的最大值
 * @param <T> 范围的类型，必须实现 Comparable 接口
 */
public record Range<T extends Comparable<T>>(T min, T max) {
    /**
     * 创建一个新的 Range 实例。最小值不能大于最大值，否则将抛出 IllegalArgumentException。
     *
     * @param min 范围的最小值
     * @param max 范围的最大值
     * @param <T> 范围的类型，必须实现 Comparable 接口
     * @return 新的 Range 实例
     */
    public static <T extends Comparable<T>> Range<T> of(T min, T max) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min 不能大于 max");
        }
        return new Range<>(min, max);
    }

    /**
     * 检查给定的值是否在范围内（包括边界）。如果值大于或等于最小值且小于或等于最大值，则返回 true；否则返回 false。
     *
     * @param value 要检查的值
     * @return 如果值在范围内（包括边界），则返回 true；否则返回 false
     */
    public boolean check(T value) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
}
