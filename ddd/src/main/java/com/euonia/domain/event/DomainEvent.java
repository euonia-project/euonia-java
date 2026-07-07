package com.euonia.domain.event;

import com.euonia.domain.Aggregate;

/**
 * {@link DomainEvent} 接口表示与领域模型中特定聚合相关联的事件类型。
 * 它继承自 {@link Event} 接口，继承事件的基础属性和方法，同时添加了领域事件特有的功能。
 * <p>
 * 领域事件用于捕获和表示在特定聚合上下文中发生的重要事件或变更。
 * 它们允许系统各组件之间的通信和协调，将聚合状态变更的重要信息传播给系统中可能关心这些变更的其他部分。
 * <p>
 * 通过将领域事件附加到聚合，可以确保事件与特定的聚合实例关联，并可相应处理。
 * {@link #getEventAggregate()} 方法允许你检索关联的 {@link EventAggregate}，其中包含事件及其在领域模型中的上下文的相关信息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface DomainEvent extends Event {

    /**
     * 将领域事件附加到指定的聚合。此方法允许你将事件与特定的聚合实例关联，使事件能够在聚合上下文中进行处理。
     *
     * @param <ID>      聚合所使用的标识符类型，必须可比较
     * @param aggregate 领域事件应附加到的聚合
     */
    <ID extends Comparable<ID>> void attach(Aggregate<ID> aggregate);

    /**
     * 检索与此领域事件关联的 {@link EventAggregate}。
     * 其中包含事件及其在领域模型中的上下文的相关信息，如事件的唯一标识符、时间戳、类型名称、事件意图、发起者类型、发起者 ID、事件负载和事件序号。
     *
     * @return 与此领域事件关联的 {@link EventAggregate}
     */
    EventAggregate getEventAggregate();
}
