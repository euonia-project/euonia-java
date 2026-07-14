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

    /**
     * 设置时间戳（支持多种输入格式：{@link Instant}、{@code String}、{@code long}、{@link LocalDateTime}、{@link ZonedDateTime}）。
     *
     * @param timestamp 时间戳，可以是 {@link Instant}、{@code String}、{@code long}、{@link LocalDateTime} 或 {@link ZonedDateTime} 类型
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = Instant.parse(timestamp).toEpochMilli();
    }

    /**
     * 设置时间戳（支持多种输入格式：{@link Instant}、{@code String}、{@code long}、{@link LocalDateTime}、{@link ZonedDateTime}）。
     *
     * @param timestamp 时间戳，单位为毫秒
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 设置时间戳（支持多种输入格式：{@link Instant}、{@code String}、{@code long}、{@link LocalDateTime}、{@link ZonedDateTime}）。
     *
     * @param timestamp 时间戳，可以是 {@link LocalDateTime} 类型
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * 设置时间戳（支持多种输入格式：{@link Instant}、{@code String}、{@code long}、{@link LocalDateTime}、{@link ZonedDateTime}）。
     *
     * @param timestamp 时间戳，可以是 {@link ZonedDateTime} 类型
     */
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp.toInstant().toEpochMilli();
    }

    /**
     * 获取事件类型名称。
     *
     * @return 事件类型名称
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 设置事件类型名称。
     *
     * @param typeName 事件类型名称
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 获取事件意图。
     *
     * @return 事件意图
     */
    public String getEventIntent() {
        return eventIntent;
    }

    /**
     * 设置事件意图。
     *
     * @param eventIntent 事件意图
     */
    public void setEventIntent(String eventIntent) {
        this.eventIntent = eventIntent;
    }

    /**
     * 获取事件发起者类型。
     *
     * @return 事件发起者类型
     */
    public String getOriginatorType() {
        return originatorType;
    }

    /**
     * 设置事件发起者类型。
     *
     * @param originatorType 事件发起者类型
     */
    public void setOriginatorType(String originatorType) {
        this.originatorType = originatorType;
    }

    /**
     * 获取事件发起者 ID。
     *
     * @return 事件发起者 ID
     */
    public String getOriginatorId() {
        return originatorId;
    }

    /**
     * 设置事件发起者 ID。
     *
     * @param originatorId 事件发起者 ID
     */
    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    /**
     * 获取事件负载。
     *
     * @return 事件负载
     */
    public Object getEventPayload() {
        return eventPayload;
    }

    /**
     * 设置事件负载。
     *
     * @param eventPayload 事件负载
     */
    public void setEventPayload(Object eventPayload) {
        this.eventPayload = eventPayload;
    }

    /**
     * 获取事件序列号。
     *
     * @return 事件序列号
     */
    public long getEventSequence() {
        return eventSequence;
    }

    /**
     * 设置事件序列号。
     *
     * @param eventSequence 事件序列号
     */
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
