package com.euonia.domain;

/**
 * 实体的抽象基类实现，提供了 {@link Entity} 接口的基础实现。
 * <p>
 * 包含 ID 的 getter/setter、默认的键数组和 {@code toString} 方法。
 *
 * @param <ID> 实体标识符的类型
 * @author damon(zhaorong@outlook.com)
 */
public abstract class EntityBase<ID extends Comparable<ID>> implements Entity<ID> {
    /** 实体标识符 */
    private ID id;

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public Object[] getKeys() {
        return new Object[]{id};
    }

    @Override
    public String toString() {
        return String.format("%s{id=%s}", this.getClass().getSimpleName(), id);
    }
}
