package com.euonia.domain;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 值对象的基类，提供了 {@code equals}、{@code hashCode} 和 {@code compareTo} 方法的实现。
 * <p>
 * 通过反射比较对象的所有声明字段。{@code compareTo} 按字段顺序比较，第一个不相等的字段决定排序结果；{@code equals} 要求所有字段相等；{@code hashCode} 使用交替乘数计算哈希值。
 *
 * @param <T> 值对象的类型
 * @author damon(zhaorong@outlook.com)
 */
public class ValueObject<T extends ValueObject<T>> implements Comparable<T> {
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(T other) {
        if (other == null) {
            return 1;
        }

        var fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                var left = field.get(this);
                var right = field.get(other);

                if (left == right) {
                    continue;
                }

                if (left == null) {
                    return -1;
                }

                if (right == null) {
                    return 1;
                }

                if (left instanceof Comparable && right instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    int result = ((Comparable<Object>) left).compareTo(right);
                    if (result != 0) {
                        return result;
                    }
                } else {
                    int result = left.toString().compareTo(right.toString());
                    if (result != 0) {
                        return result;
                    }
                }

            } catch (IllegalAccessException | IllegalArgumentException exception) {
                return 0;
            }
        }
        return 0;
    }

    public boolean equals(T obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        var fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                var left = field.get(this);
                var right = field.get(obj);

                if (left == right) {
                    continue;
                }

                if (left == null || right == null) {
                    return false;
                }

                if (!Objects.equals(left, right)) {
                    return false;
                }

            } catch (IllegalAccessException | IllegalArgumentException exception) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return equals((T) obj);
    }

    @Override
    public int hashCode() {
        var hashCode = 31;
        var changeMultiplier = false;
        var fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                var value = field.get(this);
                if (value != null) {
                    hashCode = hashCode * (changeMultiplier ? 59 : 114) + value.hashCode();
                    changeMultiplier = !changeMultiplier;
                } else {
                    hashCode ^= 13;
                }
            } catch (IllegalAccessException | IllegalArgumentException ignored) {
            }
        }
        return hashCode;
    }
}
