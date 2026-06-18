package com.euonia.core;

/**
 * 一个表示键值对的泛型类。键必须是可比较的，以确保可以基于键对键值对进行排序。
 *
 * @param key   键值对的键
 * @param value 与键关联的值
 * @param <K>   键的类型
 * @param <V>   值的类型
 * @author damon(zhaorong@outlook.com)
 */
public record Pair<K extends Comparable<K>, V>(K key, V value) {
    public static <K extends Comparable<K>, V> Pair<K, V> of(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("key 为 null");
        }
        return new Pair<>(key, value);
    }
}
