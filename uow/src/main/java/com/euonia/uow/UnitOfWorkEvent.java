package com.euonia.uow;

/**
 * 工作单元完成或被释放时引发的事件。
 *
 * <p>通过 {@link UnitOfWork#addCompletedListener} 和
 * {@link UnitOfWork#addDisposedListener} 注册的监听器会收到此类的实例。</p>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWorkFailure
 */
public class UnitOfWorkEvent {
    private final UnitOfWork unitOfWork;

    /**
     * 为给定的工作单元创建新的事件。
     *
     * @param unitOfWork 触发事件的工作单元
     */
    public UnitOfWorkEvent(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    /**
     * 返回触发此事件的工作单元。
     *
     * @return 关联的工作单元
     */
    public UnitOfWork getUnitOfWork() {
        return unitOfWork;
    }
}
