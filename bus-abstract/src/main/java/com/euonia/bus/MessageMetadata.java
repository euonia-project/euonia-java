package com.euonia.bus;

import com.euonia.core.ArgumentNullException;
import com.euonia.utility.Resource;

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
@SuppressWarnings("NullableProblems")
public class MessageMetadata implements Map<String, Object> {
    /**
     * 底层元数据存储
     */
    private final Map<String, Object> metadata = new HashMap<>();

    @Override
    public int size() {
        return metadata.size();
    }

    /**
     * 检查元数据映射是否为空。
     *
     * @return 如果元数据映射为空，则返回 true；否则返回 false
     */
    @Override
    public boolean isEmpty() {
        return metadata.isEmpty();
    }

    /**
     * 检查元数据映射中是否包含指定的键。
     *
     * @param key 要检查的键
     * @return 如果元数据映射中包含指定的键，则返回 true；否则返回 false
     */
    @Override
    public boolean containsKey(Object key) {
        ArgumentNullException.throwIfNull(key, "key");
        return metadata.containsKey(key);
    }

    /**
     * 检查元数据映射中是否包含指定的值。
     *
     * @param value 要检查的值
     * @return 如果元数据映射中包含指定的值，则返回 true；否则返回 false
     */
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
        ArgumentNullException.throwIfNull(key, "key");
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
        throw new IllegalStateException(Resource.getString("resource", "MessageMetadata.ValueOfKeyIsNotTypeOf", key, type.getName()));
    }

    /**
     * 根据键获取元数据值，如果键不存在则返回 null。
     *
     * @param key 要获取的键
     * @return 键对应的值，如果键不存在则返回 null
     */
    @Override
    public Object get(Object key) {
        ArgumentNullException.throwIfNull(key, "key");
        return metadata.get(key);
    }

    /**
     * 将指定键值对存储到元数据映射中。
     *
     * @param key   元数据键
     * @param value 元数据值
     * @return 之前与键关联的值，如果键不存在则返回 null
     */
    @Override
    public Object put(String key, Object value) {
        ArgumentNullException.throwIfNullOrEmpty(key, "key");
        return metadata.put(key, value);
    }

    /**
     * 从元数据映射中移除指定键的映射关系。
     *
     * @param key 要移除的键
     * @return 移除的键对应的值，如果键不存在则返回 null
     */
    @Override
    public Object remove(Object key) {
        ArgumentNullException.throwIfNull(key, "key");
        return metadata.remove(key);
    }

    /**
     * 将指定映射中的所有键值对存储到此元数据映射中。
     *
     * @param map 要存储到此映射中的映射
     */
    @Override
    public void putAll(Map<? extends String, ?> map) {
        ArgumentNullException.throwIfNull(map, "map");
        metadata.putAll(map);
    }

    /**
     * 清空元数据映射中的所有键值对。
     */
    @Override
    public void clear() {
        metadata.clear();
    }

    /**
     * 获取元数据映射中的所有键。
     *
     * @return 包含所有键的 {@link Set} 集合
     */
    @Override
    public Set<String> keySet() {
        return metadata.keySet();
    }

    /**
     * 获取元数据映射中的所有值。
     *
     * @return 包含所有值的 {@link Collection} 集合
     */
    @Override
    public Collection<Object> values() {
        return metadata.values();
    }

    /**
     * 获取元数据映射中的所有键值对。
     *
     * @return 包含所有键值对的 {@link Set} 集合
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return metadata.entrySet();
    }

}
