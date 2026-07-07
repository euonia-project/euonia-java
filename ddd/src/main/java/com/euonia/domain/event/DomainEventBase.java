package com.euonia.domain.event;

import java.time.Instant;

import com.euonia.core.ObjectId;
import com.euonia.domain.Aggregate;

/**
 * 领域事件的基类实现，具体的领域事件可继承此类。
 * 提供了处理领域事件的公共属性和方法，例如附加到聚合和转换为事件聚合。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class DomainEventBase extends EventBase implements DomainEvent {

    /**
     * 聚合负载对象
     */
    private Object aggregatePayload;

    @Override
    public <ID extends Comparable<ID>> void attach(Aggregate<ID> aggregate) {
        setOriginatorId(aggregate.getId().toString());
        setOriginatorType(aggregate.getClass().getTypeName());
        setAggregatePayload(aggregate);
    }

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
