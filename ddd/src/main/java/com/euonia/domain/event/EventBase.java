package com.euonia.domain.event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.euonia.reflection.TypeHelper;

/**
 * {@link Event} 接口的基础实现。
 * <p>
 * 为所有事件提供公共属性和方法，包括事件意图、发起者类型、发起者 ID 和事件序号。子类可以扩展此类以创建具有额外属性和行为的特定事件类型。
 * <p>
 * 事件属性存储在内部的 {@code properties} 映射中，支持通过 {@link #get(String, Class)}方法进行类型安全的属性值转换。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class EventBase implements Event {
    /**
     * 事件意图属性的存储键
     */
    private static final String PROPERTY_EVENT_INTENT = "nerosoft.euonia.internal.event.intent";
    /**
     * 发起者类型属性的存储键
     */
    private static final String PROPERTY_ORIGINATOR_TYPE = "nerosoft.euonia.internal.event.originator.type";
    /**
     * 发起者 ID 属性的存储键
     */
    private static final String PROPERTY_ORIGINATOR_ID = "nerosoft.euonia.internal.event.originator.id";
    /**
     * 事件属性存储
     */
    private final Map<String, String> properties = new HashMap<>();
    /**
     * 事件序号，基于当前时间的毫秒值
     */
    private long sequence = Instant.now().toEpochMilli();

    /**
     * 构造事件基类实例，自动将事件意图设置为当前类的全限定名。
     */
    protected EventBase() {
        var type = getClass();
        set(PROPERTY_EVENT_INTENT, type.getName());
    }

    /**
     * 根据键获取属性值，如果键不存在则返回 null。
     *
     * @param property 属性键
     * @return 属性值，如果键不存在则返回 null
     */
    public final String get(String property) {
        return properties.getOrDefault(property, null);
    }

    /**
     * 设置属性键值对。
     *
     * @param property 属性键
     * @param value    属性值
     */
    public final void set(String property, String value) {
        properties.put(property, value);
    }

    /**
     * 根据键获取属性值，并通过 {@link TypeHelper#coerceValue} 强制转换为指定类型。
     *
     * @param <T>      期望的值类型
     * @param property 属性键
     * @param castType 目标类型
     * @return 强制转换后的属性值，如果键不存在则返回 null
     */
    public <T> T get(String property, Class<T> castType) {
        var value = properties.getOrDefault(property, null);
        if (value == null) {
            return null;
        }
        return TypeHelper.coerceValue(castType, value);
    }

    @Override
    public String getEventIntent() {
        return get(PROPERTY_EVENT_INTENT);
    }

    @Override
    public void setEventIntent(String eventIntent) {
        set(PROPERTY_EVENT_INTENT, eventIntent);
    }

    @Override
    public final long getSequence() {
        return sequence;
    }

    @Override
    public final void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getOriginatorType() {
        return get(PROPERTY_ORIGINATOR_TYPE);
    }

    @Override
    public void setOriginatorType(String originatorType) {
        set(PROPERTY_ORIGINATOR_TYPE, originatorType);
    }

    @Override
    public String getOriginatorId() {
        return get(PROPERTY_ORIGINATOR_ID);
    }

    @Override
    public void setOriginatorId(String originatorId) {
        set(PROPERTY_ORIGINATOR_ID, originatorId);
    }
}
