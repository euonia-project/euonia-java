package com.euonia.domain.event;

import java.time.Instant;

import com.euonia.core.ObjectId;
import com.euonia.domain.Aggregate;

/**
 * 领域事件的基类实现，具体的领域事件可继承此类。提供了处理领域事件的公共属性和方法，例如附加到聚合和转换为事件聚合。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class DomainEventBase extends EventBase implements DomainEvent {

    /**
     * 聚合负载对象
     */
    private Object aggregatePayload;

    /**
     * 将领域事件附加到指定的聚合。此方法允许你将事件与特定的聚合实例关联，使事件能够在聚合上下文中进行处理。
     *
     * @param aggregate 领域事件应附加到的聚合
     * @param <ID>      聚合所使用的标识符类型，必须可比较
     */
    @Override
    public <ID extends Comparable<ID>> void attach(Aggregate<ID> aggregate) {
        setOriginatorId(aggregate.getId().toString());
        setOriginatorType(aggregate.getClass().getTypeName());
        setAggregatePayload(aggregate);
    }

    /**
     * 将领域事件转换为 {@link EventAggregate} 对象。
     * 该方法创建一个新的 {@link EventAggregate} 实例，并将当前领域事件的相关属性复制到该实例中，包括事件的唯一标识符、时间戳、类型名称、事件意图、发起者类型、发起者 ID、事件负载和事件序号。
     * 通过调用此方法，可以方便地将领域事件与其上下文信息封装在一个 {@link EventAggregate} 对象中，以便在系统中进行传递和处理。
     *
     * @return 转换后的 {@link EventAggregate} 对象
     */
    @Override
    public EventAggregate getEventAggregate() {
        return new EventAggregate() {{
            setId(ObjectId.guid().toString());
            setTypeName(getClass().getTypeName());
            setEventIntent(getEventIntent());
            setEventId(getEventId());
            setTimestamp(Instant.now());
            setOriginatorId(getOriginatorId());
            setOriginatorType(getOriginatorType());
            setEventSequence(getSequence());
            setAggregatePayload(this);
        }};
    }

    /**
     * 获取关联的聚合负载对象。
     *
     * @return 聚合负载
     */
    public Object getAggregatePayload() {
        return aggregatePayload;
    }

    /**
     * 设置关联的聚合负载对象。
     *
     * @param aggregatePayload 聚合负载
     */
    public void setAggregatePayload(Object aggregatePayload) {
        this.aggregatePayload = aggregatePayload;
    }

    /**
     * 获取关联的聚合，并强制转换为指定类型。
     *
     * @param <A>  期望的聚合类型
     * @param type 目标聚合类型
     * @return 强制转换后的聚合，如果聚合负载为 null 或类型不匹配则返回 null
     */
    @SuppressWarnings("unchecked")
    public <A extends Aggregate<?>> A getAggregate(Class<A> type) {
        if (aggregatePayload == null) {
            return null;
        }

        if (aggregatePayload.getClass().isAssignableFrom(type)) {
            return (A) aggregatePayload;
        }
        return null;
    }
}
