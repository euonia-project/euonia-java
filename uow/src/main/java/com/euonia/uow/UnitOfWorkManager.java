package com.euonia.uow;

import java.util.Objects;

/**
 * 创建和管理工作单元的入口点。
 *
 * <p>每个管理器持有一组默认的 {@link UnitOfWorkOptions} 和一个
 * {@link UnitOfWorkAccessor}，用于跟踪当前线程上的环境工作单元。</p>
 *
 * <pre>{@code
 * UnitOfWorkManager manager = new UnitOfWorkManager();
 * try (UnitOfWork uow = manager.begin(new UnitOfWorkOptions(true), false)) {
 *     uow.addContext("db", new JdbcTransactionContext(connection));
 *     // ... 业务逻辑 ...
 *     uow.completeAsync().toCompletableFuture().join();
 * }
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWork
 * @see ChildUnitOfWork
 */
public class UnitOfWorkManager {
    private final UnitOfWorkAccessor accessor;
    private final UnitOfWorkOptions defaultOptions;

    /** 使用默认选项和新的访问器创建管理器。 */
    public UnitOfWorkManager() {
        this(new UnitOfWorkAccessor(), new UnitOfWorkOptions());
    }

    /**
     * 使用给定的访问器和默认选项创建管理器。
     *
     * @param accessor       用于跟踪当前工作单元的访问器
     * @param defaultOptions 应用于新工作单元的默认选项
     */
    public UnitOfWorkManager(UnitOfWorkAccessor accessor, UnitOfWorkOptions defaultOptions) {
        this.accessor = Objects.requireNonNull(accessor, "accessor");
        this.defaultOptions = defaultOptions == null ? new UnitOfWorkOptions() : defaultOptions;
    }

    /**
     * 返回与当前线程关联的工作单元，如果没有活跃的工作单元则返回 {@code null}。
     *
     * @return 当前工作单元，可能为 {@code null}
     */
    public UnitOfWork getCurrent() {
        return accessor.getCurrentUnitOfWork();
    }

    /**
     * 使用给定的选项开启一个新的工作单元。
     *
     * <p>如果当前线程上已有活跃的工作单元且 {@code requiresNew} 为 {@code false}，
     * 则返回一个委托给现有单元的 {@link ChildUnitOfWork}。
     * 否则创建一个新的顶级单元并将其设置为环境工作单元。</p>
     *
     * @param options     工作单元的选项
     * @param requiresNew 如果为 {@code true}，始终创建新的顶级单元
     * @return 工作单元（必须通过 {@link UnitOfWork#close()} 关闭）
     */
    public UnitOfWork begin(UnitOfWorkOptions options, boolean requiresNew) {
        if (options == null) {
            throw new IllegalArgumentException("options");
        }

        UnitOfWork current = getCurrent();
        if (current != null && !requiresNew) {
            return new ChildUnitOfWork(current);
        }

        UnitOfWork unitOfWork = new UnitOfWork(defaultOptions);
        unitOfWork.initialize(options);
        unitOfWork.setOuter(current);
        accessor.setCurrentUnitOfWork(unitOfWork);
        unitOfWork.addDisposedListener(event -> accessor.setCurrentUnitOfWork(current));
        return unitOfWork;
    }

    /**
     * 使用事务性标志开启工作单元的便捷方法。
     *
     * @param transactional 工作单元是否具有事务性
     * @param requiresNew   如果为 {@code true}，始终创建新的顶级单元
     * @return 工作单元
     */
    public UnitOfWork begin(boolean transactional, boolean requiresNew) {
        return begin(new UnitOfWorkOptions(transactional), requiresNew);
    }
}
