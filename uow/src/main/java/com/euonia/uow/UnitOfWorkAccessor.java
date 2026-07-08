package com.euonia.uow;

/**
 * 当前线程环境 {@link UnitOfWork} 的线程本地持有者。
 *
 * <p>每个线程都有自己独立的工作单元，因此在 servlet 容器等线程-请求环境下使用是安全的。</p>
 *
 * <pre>{@code
 * UnitOfWork current = accessor.getCurrentUnitOfWork();
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UnitOfWorkAccessor {
    private final ThreadLocal<UnitOfWork> current = new ThreadLocal<>();

    /**
     * 返回与当前线程关联的工作单元。如果没有活跃的工作单元则返回 {@code null}。
     *
     * @return 当前工作单元，可能为 {@code null}
     */
    public UnitOfWork getCurrentUnitOfWork() {
        return current.get();
    }

    /**
     * 为当前线程设置工作单元。传入 {@code null} 将移除关联。
     *
     * @param unitOfWork 要关联的工作单元，传入 {@code null} 以清除
     */
    public void setCurrentUnitOfWork(UnitOfWork unitOfWork) {
        if (unitOfWork == null) {
            current.remove();
            return;
        }

        current.set(unitOfWork);
    }
}
