package com.euonia.domain;

import java.util.List;
import java.util.function.Consumer;

import com.euonia.domain.event.DomainEvent;

/**
 * {@link HasDomainEvents} 接口定义了在领域驱动设计（DDD）上下文中可引发和管理领域事件的聚合的契约。
 * <p>
 * 聚合是一组领域对象，可以作为数据变更的单一单元处理。
 * 聚合根是控制对聚合内其他实体访问的主实体，负责保证整个聚合的一致性。
 * 领域事件表示聚合状态中与业务逻辑相关的重大事件或变更，可用于事件溯源、审计或与其他系统集成。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface HasDomainEvents {
    /**
     * 获取此聚合已引发的领域事件列表。
     * 领域事件表示聚合状态中与业务逻辑相关的重大事件或变更，
     * 可用于事件溯源、审计或与其他系统集成。
     *
     * @return 此聚合引发的领域事件列表
     */
    List<DomainEvent> getEvents();

    /**
     * 注册特定类型领域事件的处理器。当引发指定类型的事件时，处理器将被调用。
     *
     * @param <E>       领域事件类型
     * @param eventType 要注册处理器的领域事件类型
     * @param handler   当事件引发时要调用的处理器
     */
    <E extends DomainEvent> void registerEvent(Class<E> eventType, Consumer<E> handler);

    /**
     * 引发领域事件。事件将由已注册的处理器处理。
     *
     * @param <E>   领域事件类型
     * @param event 要引发的领域事件
     */
    <E extends DomainEvent> void raiseEvent(E event);

    /**
     * 将领域事件应用到聚合。此方法通常用于根据事件更新聚合的状态。
     *
     * @param <E>   领域事件类型
     * @param event 要应用的领域事件
     */
    <E extends DomainEvent> void applyEvent(E event);

    /**
     * 清除此聚合已引发的所有领域事件。
     */
    void clearEvents();

    /**
     * 将所有领域事件附加到聚合。此方法通常用于使用现有事件初始化聚合。
     */
    void attachEvents();
}
