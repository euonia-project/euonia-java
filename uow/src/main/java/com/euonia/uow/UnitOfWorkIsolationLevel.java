package com.euonia.uow;

import java.sql.Connection;

/**
 * 事务隔离级别，与 JDBC 隔离常量兼容。
 *
 * <ul>
 *   <li>{@link #UNSPECIFIED} —— 使用底层存储的默认值</li>
 *   <li>{@link #READ_UNCOMMITTED} —— 允许脏读</li>
 *   <li>{@link #READ_COMMITTED} —— 防止脏读（最常用）</li>
 *   <li>{@link #REPEATABLE_READ} —— 事务内一致读取</li>
 *   <li>{@link #SERIALIZABLE} —— 最严格的隔离</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 * @see java.sql.Connection#TRANSACTION_NONE
 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
 */
public enum UnitOfWorkIsolationLevel {
    /**
     * 使用底层存储的默认隔离级别。
     */
    UNSPECIFIED(Connection.TRANSACTION_NONE),
    /**
     * 混沌隔离 —— 映射到 {@link Connection#TRANSACTION_NONE}。
     */
    CHAOS(Connection.TRANSACTION_NONE),
    /**
     * 可能出现脏读、不可重复读和幻读。
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /**
     * 防止脏读；可能出现不可重复读和幻读。
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /**
     * 防止脏读和不可重复读；可能出现幻读。
     */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    /**
     * 快照隔离 —— 映射到 {@link Connection#TRANSACTION_REPEATABLE_READ}。
     */
    SNAPSHOT(Connection.TRANSACTION_REPEATABLE_READ),
    /**
     * 防止脏读、不可重复读和幻读。
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int jdbcIsolationLevel;

    UnitOfWorkIsolationLevel(int jdbcIsolationLevel) {
        this.jdbcIsolationLevel = jdbcIsolationLevel;
    }

    /**
     * 将此隔离级别转换为对应的 JDBC 常量。
     *
     * @return {@link Connection} 事务隔离常量
     */
    public int toJdbcIsolationLevel() {
        return jdbcIsolationLevel;
    }
}
