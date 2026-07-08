package com.euonia.uow;

import java.util.concurrent.CompletionStage;

/**
 * 参与工作单元的事务资源（数据库、消息代理等）的抽象。
 *
 * <p>实现类通过 {@link UnitOfWork#addContext} 向 {@link UnitOfWork} 注册自己，
 * 并接收生命周期回调：</p>
 * <ol>
 *   <li>{@link #saveChangesAsync()} —— 刷新挂起的变更</li>
 *   <li>{@link #commitAsync()} —— 提交事务</li>
 *   <li>{@link #rollbackAsync()} —— 回滚事务</li>
 *   <li>{@link #close()} —— 释放资源</li>
 * </ol>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWork
 */
public interface UnitOfWorkContext {
    /**
     * 将挂起的变更持久化到底层资源，但不提交事务。
     *
     * @return 在变更保存后完成的阶段
     */
    CompletionStage<Void> saveChangesAsync();

    /**
     * 提交事务。
     *
     * @return 在提交完成后完成的阶段
     */
    CompletionStage<Void> commitAsync();

    /**
     * 回滚事务。
     *
     * @return 在回滚完成后完成的阶段
     */
    CompletionStage<Void> rollbackAsync();

    /**
     * 释放此上下文持有的所有资源。
     * 默认实现不做任何操作。
     */
    default void close() {
    }
}
