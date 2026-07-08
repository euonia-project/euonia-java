package com.euonia.uow;

import java.time.Duration;

/**
 * 工作单元的配置选项。
 *
 * <p>选项控制事务行为、隔离级别和超时时间。
 * 使用 {@link #normalize(UnitOfWorkOptions)} 与默认选项进行合并。</p>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWork
 * @see UnitOfWorkManager
 */
public class UnitOfWorkOptions {
    private boolean transactional;
    private UnitOfWorkIsolationLevel isolationLevel;
    private Duration timeout;

    /** 创建默认的、非事务性的选项。 */
    public UnitOfWorkOptions() {
    }

    /**
     * 使用给定的事务性标志创建选项。
     *
     * @param transactional 工作单元是否具有事务性
     */
    public UnitOfWorkOptions(boolean transactional) {
        this.transactional = transactional;
    }

    /**
     * 创建完全指定的选项。
     *
     * @param transactional  工作单元是否具有事务性
     * @param isolationLevel 事务隔离级别，可以为 {@code null}
     * @param timeout        事务超时时间，可以为 {@code null}
     */
    public UnitOfWorkOptions(boolean transactional, UnitOfWorkIsolationLevel isolationLevel, Duration timeout) {
        this.transactional = transactional;
        this.isolationLevel = isolationLevel;
        this.timeout = timeout;
    }

    /**
     * 返回工作单元是否应为事务性的。
     *
     * @return 如果是事务性的则返回 {@code true}
     */
    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    /**
     * 返回事务隔离级别，如果未设置则返回 {@code null}。
     *
     * @return 隔离级别，可能为 {@code null}
     */
    public UnitOfWorkIsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(UnitOfWorkIsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    /**
     * 返回事务超时时间，如果未设置则返回 {@code null}。
     *
     * @return 超时时间，可能为 {@code null}
     */
    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * 将给定的选项与此实例合并，使用此实例的值作为任何 {@code null} 字段的默认值。
     *
     * @param options 要规范化的选项（不能为 {@code null}）
     * @return 规范化后的选项对象（同一实例）
     */
    public UnitOfWorkOptions normalize(UnitOfWorkOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("options");
        }

        if (options.isolationLevel == null) {
            options.isolationLevel = isolationLevel;
        }

        if (options.timeout == null) {
            options.timeout = timeout;
        }

        return options;
    }
}
