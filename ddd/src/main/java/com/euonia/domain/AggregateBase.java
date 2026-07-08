package com.euonia.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import com.euonia.domain.event.DomainEvent;

/**
 * {@link AggregateBase} 是领域驱动设计（DDD）上下文中实现 {@link Aggregate} 接口的抽象基类。
 * 提供了在聚合根内管理领域事件及其处理器的公共实现。聚合是一组领域对象，可以作为数据变更的单一单元处理。聚合根是控制对聚合内其他实体访问的主实体，负责保证整个聚合的一致性。
 *
 * @param <ID> 聚合标识符的类型，必须可比较
 * @author damon(zhaorong@outlook.com)
 */
public abstract class AggregateBase<ID extends Comparable<ID>> extends EntityBase<ID> implements Aggregate<ID>, HasDomainEvents {
    /**
     * 已引发的领域事件列表
     */
    private final List<DomainEvent> events = new ArrayList<>();
    /**
     * 事件类型到处理器列表的映射
     */
    private final ConcurrentMap<Class<? extends DomainEvent>, List<Consumer<DomainEvent>>> eventHandlers = new ConcurrentHashMap<>();

    /**
     * 注册特定类型领域事件的处理器。当引发指定类型的事件时，处理器将被调用。
     *
     * @param <E>       领域事件类型
     * @param eventType 要注册处理器的事件类型
     * @param handler   当事件引发时要调用的处理器
     */
    public <E extends DomainEvent> void registerEvent(Class<E> eventType, Consumer<E> handler) {
        eventHandlers.computeIfAbsent(eventType, k -> new ArrayList<>())
                     .add(event -> handler.accept(eventType.cast(event)));
    }

    @Override
    public List<DomainEvent> getEvents() {
        return List.copyOf(events);
    }

    @Override
    public <E extends DomainEvent> void raiseEvent(E event) {
        applyEvent(event);
        events.add(event);
    }

    /**
     * 应用领域事件，调用注册的处理器来处理该事件。
     *
     * @param event 要应用的领域事件
     * @param <E>   领域事件类型
     */
    @Override
    public <E extends DomainEvent> void applyEvent(E event) {
        var handlers = eventHandlers.getOrDefault(event.getClass(), null);
        if (handlers != null) {
            handlers.forEach(handler -> handler.accept(event));
        }
    }

    /**
     * 清除已引发的领域事件列表。此方法通常在事件处理完成后调用，以便重置聚合的事件状态。
     */
    @Override
    public void clearEvents() {
        events.clear();
    }

    /**
     * 将所有已引发的领域事件附加到当前聚合实例。此方法会遍历已引发的事件列表，并调用每个事件的 attach 方法，将其与当前聚合关联起来。
     */
    @Override
    public void attachEvents() {
        for (DomainEvent event : events) {
            event.attach(this);
        }
    }
}
