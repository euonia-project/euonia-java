package com.euonia.domain;

/**
 * {@link Aggregate} 接口表示领域驱动设计（DDD）上下文中的聚合根。
 * <p>
 * 聚合是一组领域对象，可以作为数据变更的单一单元处理。聚合根是控制对聚合内其他实体访问的主实体，负责保证整个聚合的一致性。
 *
 * @param <ID> 聚合标识符的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface Aggregate<ID extends Comparable<ID>> extends Entity<ID> {

}
