package com.euonia.bus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 消息元数据容器，封装了一个 {@link Map} 用于存储消息的附加元数据。
 * <p>
 * 除了实现 {@link Map} 接口外，还提供了类型安全的 {@link #get(String, Class)} 方法，
 * 在获取值时进行类型检查，如果类型不匹配则抛出 {@link IllegalStateException}。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageMetadata implements Map<String, Object> {
    /**
     * 底层元数据存储
     */
    private final Map<String, Object> metadata = new HashMap<>();

    @Override
    public int size() {
        return metadata.size();
    }

    @Override
    public boolean isEmpty() {
        return metadata.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return metadata.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return metadata.containsValue(value);
    }

    /**
     * 根据键获取元数据值，如果键不存在则返回 null。
     *
     * @param key 元数据键
     * @return 元数据值，如果键不存在则返回 null
     */
    public Object get(String key) {
        return metadata.getOrDefault(key, null);
    }

    /**
     * 根据键获取元数据值，并在返回值时进行类型强制转换。
     * <p>
     * 如果键不存在则返回 null；如果值类型不匹配则抛出 {@link IllegalStateException}。
     *
     * @param <T>  期望的值类型
     * @param key  元数据键
     * @param type 期望的值类型
     * @return 强制转换为指定类型的元数据值，如果键不存在则返回 null
     * @throws IllegalStateException 如果值存在但类型不匹配
     */
    public <T> T get(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalStateException(
            String.format("Value for key '%s' is not of type %s", key, type.getName()));
    }

    @Override
    public Object get(Object key) {
        return metadata.getOrDefault(key, null);
    }

    @Override
    public Object put(String key, Object value) {
        return metadata.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return metadata.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        metadata.putAll(m);
    }

    @Override
    public void clear() {
        metadata.clear();
    }

    @Override
    public Set<String> keySet() {
        return metadata.keySet();
    }

    @Override
    public Collection<Object> values() {
        return metadata.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return metadata.entrySet();
    }

}
