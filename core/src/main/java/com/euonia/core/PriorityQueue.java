package com.euonia.core;

import java.util.Comparator;

/**
 * PriorityQueue 是一个基于 Java 内置优先级队列实现的通用优先级队列类。
 * 它使用一个 Pair 类来存储元素和其对应的优先级，并通过 Comparator 来确保队列按照优先级排序。
 * 该类提供了基本的队列操作方法，如添加元素、检索和移除最高优先级元素、查看队列头部元素、清空队列、获取队列大小以及检查队列是否为空。
 *
 * @param <E> 元素类型
 * @param <K> 优先级类型，必须实现 Comparable 接口
 * @author damon(zhaorong@outlook)
 */
public class PriorityQueue<E, K extends Comparable<K>> {
    private final java.util.PriorityQueue<Pair<K, E>> queue;

    public PriorityQueue() {
        this.queue = new java.util.PriorityQueue<>(Comparator.comparing(Pair::key));
    }

    /**
     * 将指定元素以给定的优先级插入此优先级队列。
     *
     * @param value    要添加的元素
     * @param priority 元素的优先级
     */
    public void add(E value, K priority) {
        queue.add(new Pair<>(priority, value));
    }

    /**
     * 检索并移除队列头部元素，如果队列为空则返回 null。
     *
     * @return 最高优先级元素的值，如果队列为空则返回 null
     */
    public E poll() {
        Pair<K, E> pair = queue.poll();
        return pair != null ? pair.value() : null;
    }

    /**
     * 检索但不移除队列头部元素。此方法与 peek 的区别仅在于：如果队列为空，它将抛出异常。
     *
     * @return 最高优先级元素的值，如果队列为空则返回 null
     */
    public E peek() {
        Pair<K, E> pair = queue.peek();
        return pair != null ? pair.value() : null;
    }

    /**
     * 从队列中移除所有元素。
     */
    public void clear() {
        queue.clear();
    }

    /**
     * 返回队列中的元素数量。
     *
     * @return 队列中的元素数量
     */
    public int size() {
        return queue.size();
    }

    /**
     * 检查队列是否为空。
     *
     * @return 如果队列为空则返回 true，否则返回 false
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
