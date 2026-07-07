package com.euonia.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.euonia.domain.Aggregate;

/**
 * 事件聚合实体，实现了 {@link Aggregate} 接口，用于封装领域事件的元数据和上下文信息。
 * <p>
 * 包含事件的 ID、类型名称、事件意图、发起者信息、时间戳、事件负载和序号等属性。
 * 这些信息用于事件的持久化、查询和事件溯源。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class EventAggregate implements Aggregate<String> {

    /**
     * 聚合 ID
     */
    private String id;
    /**
     * 事件 ID
     */
    private String eventId;
    /**
     * 事件时间戳（Unix 毫秒）
     */
    private long timestamp;
    /**
     * 事件类型名称
     */
    private String typeName;
    /**
     * 事件意图
     */
    private String eventIntent;
    /**
     * 发起者类型
     */
    private String originatorType;
    /**
     * 发起者 ID
     */
    private String originatorId;
    /**
     * 事件负载对象
     */
    private Object eventPayload;
    /**
     * 事件序号
     */
    private long eventSequence;

    /**
     * 获取聚合的唯一标识符。
     *
     * @return 聚合的唯一标识符
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * 设置聚合的唯一标识符。
     *
     * @param id 聚合的唯一标识符
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取事件 ID，用于唯一标识事件。
     *
     * @return 事件 ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 设置事件 ID，用于唯一标识事件。
     *
     * @param eventId 事件 ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 获取时间戳（Unix 毫秒）
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置时间戳（支持多种输入格式：{@link Instant}、{@code String}、{@code long}、{@link LocalDateTime}、{@link ZonedDateTime}）。
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp.toEpochMilli();
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Instant.parse(timestamp).toEpochMilli();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp.toInstant().toEpochMilli();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getEventIntent() {
        return eventIntent;
    }

    public void setEventIntent(String eventIntent) {
        this.eventIntent = eventIntent;
    }

    public String getOriginatorType() {
        return originatorType;
    }

    public void setOriginatorType(String originatorType) {
        this.originatorType = originatorType;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public Object getEventPayload() {
        return eventPayload;
    }

    public void setEventPayload(Object eventPayload) {
        this.eventPayload = eventPayload;
    }

    public long getEventSequence() {
        return eventSequence;
    }

    public void setEventSequence(long eventSequence) {
        this.eventSequence = eventSequence;
    }

    /**
     * 获取聚合的唯一键数组。
     *
     * @return 包含聚合 ID 的数组
     */
    @Override
    public Object[] getKeys() {
        return new Object[]{id};
    }

    @Override
    public String toString() {
        return eventIntent;
    }
}
