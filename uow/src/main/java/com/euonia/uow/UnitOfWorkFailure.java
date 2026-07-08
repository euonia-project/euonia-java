package com.euonia.uow;

/**
 * 工作单元失败时引发的事件 —— 可能由于异常或显式回滚导致。
 *
 * <p>通过 {@link UnitOfWork#addFailedListener} 注册的监听器会收到此类的实例。</p>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWorkEvent
 */
public class UnitOfWorkFailure extends UnitOfWorkEvent {
    private final Throwable exception;
    private final boolean rollback;

    /**
     * 创建一个新的失败事件。
     *
     * @param unitOfWork 失败的工作单元
     * @param exception  导致失败的异常，可能为 {@code null}
     * @param rollback   是否触发了回滚
     */
    public UnitOfWorkFailure(UnitOfWork unitOfWork, Throwable exception, boolean rollback) {
        super(unitOfWork);
        this.exception = exception;
        this.rollback = rollback;
    }

    /**
     * 返回导致失败的异常。如果失败是由显式回滚触发的，则返回 {@code null}。
     *
     * @return 异常，可能为 {@code null}
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * 返回是否执行了回滚。
     *
     * @return 如果工作单元已回滚则返回 {@code true}
     */
    public boolean isRollback() {
        return rollback;
    }
}
