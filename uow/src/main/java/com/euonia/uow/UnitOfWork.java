package com.euonia.uow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 在单个原子单元内协调事务资源。
 *
 * <h3>生命周期</h3>
 * <ol>
 *   <li>{@link #initialize(UnitOfWorkOptions)} —— 由管理器调用</li>
 *   <li>通过 {@link #addContext} 或 {@link #getOrAddContext} 注册上下文</li>
 *   <li>{@link #completeAsync()} —— 保存变更并提交</li>
 *   <li>{@link #close()} —— 释放资源（实现 {@link AutoCloseable}）</li>
 * </ol>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * try (UnitOfWork uow = new UnitOfWork()) {
 *     uow.initialize(new UnitOfWorkOptions(true));
 *     uow.addContext("db", new JdbcTransactionContext(connection));
 *     // ... 业务逻辑 ...
 *     uow.completeAsync().toCompletableFuture().join();
 * } // 自动释放
 * }</pre>
 *
 * <h3>监听器</h3>
 * <p>注册成功、失败和释放的回调：
 * <ul>
 *   <li>{@link #addCompletedListener(Consumer)}</li>
 *   <li>{@link #addFailedListener(Consumer)}</li>
 *   <li>{@link #addDisposedListener(Consumer)}</li>
 *   <li>{@link #onCompleted(Supplier)} —— 完成事件前的异步处理器</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWorkManager
 * @see UnitOfWorkContext
 * @see ChildUnitOfWork
 */
public class UnitOfWork implements AutoCloseable {
    private final String id = UUID.randomUUID().toString();
    private final Map<String, Object> items = new ConcurrentHashMap<>();
    private final Map<String, UnitOfWorkContext> contexts = new ConcurrentHashMap<>();
    private final List<Supplier<CompletionStage<Void>>> completedHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<UnitOfWorkEvent>> completedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<UnitOfWorkFailure>> failedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<UnitOfWorkEvent>> disposedListeners = new CopyOnWriteArrayList<>();

    private final UnitOfWorkOptions defaultOptions;

    private volatile boolean completing;
    private volatile boolean completed;
    private volatile boolean disposed;
    private volatile boolean rolledBack;
    private volatile Throwable failure;

    private UnitOfWorkOptions options;
    private UnitOfWork outer;
    private boolean reserved;
    private String reservationName;

    /** 使用默认（非事务性）选项创建工作单元。 */
    public UnitOfWork() {
        this(new UnitOfWorkOptions());
    }

    /**
     * 使用给定的默认选项创建工作单元。
     *
     * @param defaultOptions 回退选项；如果为 {@code null}，则使用非事务性默认值
     */
    public UnitOfWork(UnitOfWorkOptions defaultOptions) {
        this.defaultOptions = defaultOptions == null ? new UnitOfWorkOptions() : defaultOptions;
    }

    /** @return 此工作单元的唯一标识符 */
    public String getId() {
        return id;
    }

    /**
     * 返回一个可变的映射，用于存储此工作单元范围内的任意数据。
     *
     * @return 数据项映射
     */
    public Map<String, Object> getItems() {
        return items;
    }

    /**
     * 返回已注册的事务上下文的不可修改视图。
     *
     * @return 上下文映射
     */
    public Map<String, UnitOfWorkContext> getContexts() {
        return Collections.unmodifiableMap(contexts);
    }

    /** @return 活跃的选项，初始化前为 {@code null} */
    public UnitOfWorkOptions getOptions() {
        return options;
    }

    /** @return 外部（父级）工作单元，如果当前已是最外层则返回 {@code null} */
    public UnitOfWork getOuter() {
        return outer;
    }

    /** @return 此工作单元是否已被预留 */
    public boolean isReserved() {
        return reserved;
    }

    /** @return 预留名称，如果未被预留则返回 {@code null} */
    public String getReservationName() {
        return reservationName;
    }

    /** @return {@link #close()} 是否已被调用 */
    public boolean isDisposed() {
        return disposed;
    }

    /** @return {@link #completeAsync()} 是否已成功完成 */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * 返回导致此工作单元失败的异常，如果成功则返回 {@code null}。
     *
     * @return 失败异常，可能为 {@code null}
     */
    public Throwable getFailure() {
        return failure;
    }

    /**
     * 注册一个监听器，当工作单元成功完成时接收通知。
     *
     * @param listener 回调函数
     */
    public void addCompletedListener(Consumer<UnitOfWorkEvent> listener) {
        completedListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * 注册一个监听器，当工作单元失败时接收通知。
     *
     * @param listener 回调函数
     */
    public void addFailedListener(Consumer<UnitOfWorkFailure> listener) {
        failedListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * 注册一个监听器，当工作单元被释放时接收通知。
     *
     * @param listener 回调函数
     */
    public void addDisposedListener(Consumer<UnitOfWorkEvent> listener) {
        disposedListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * 注册一个异步处理器，在完成事件触发之前运行。处理器按顺序链式执行。
     *
     * @param handler 异步处理器
     */
    public void onCompleted(Supplier<CompletionStage<Void>> handler) {
        completedHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    /**
     * 使用给定的选项初始化工作单元（由管理器调用一次）。
     * 将提供的选项与此工作单元的默认选项合并。
     *
     * @param options 此工作单元的选项
     * @throws IllegalArgumentException 如果 {@code options} 为 {@code null}
     * @throws IllegalStateException    如果已经初始化
     */
    public void initialize(UnitOfWorkOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("options");
        }

        if (this.options != null) {
            throw new IllegalStateException("This unit of work is already initialized before!");
        }

        this.options = defaultOptions.normalize(options);
        this.reserved = false;
    }

    /**
     * 为此工作单元预留一个特定目的。
     *
     * @param reservationName 描述性名称
     */
    public void reserve(String reservationName) {
        if (reservationName == null || reservationName.isBlank()) {
            throw new IllegalArgumentException("reservationName");
        }

        this.reservationName = reservationName;
        this.reserved = true;
    }

    /**
     * 保存所有已注册上下文中挂起的变更，但不提交。
     *
     * @return 当所有上下文保存完成后完成的阶段
     */
    public CompletionStage<Void> saveChangesAsync() {
        if (rolledBack) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletionStage<Void>> stages = new ArrayList<>();
        for (UnitOfWorkContext context : contexts.values()) {
            stages.add(context.saveChangesAsync());
        }

        return CompletableFuture.allOf(stages.stream()
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture[]::new));
    }

    /**
     * 回滚所有已注册的上下文。
     *
     * @return 当所有上下文回滚完成后完成的阶段
     */
    public CompletionStage<Void> rollbackAsync() {
        if (rolledBack) {
            return CompletableFuture.completedFuture(null);
        }

        rolledBack = true;
        List<CompletionStage<Void>> stages = new ArrayList<>();
        for (UnitOfWorkContext context : contexts.values()) {
            stages.add(context.rollbackAsync());
        }

        return CompletableFuture.allOf(stages.stream()
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture[]::new));
    }

    /**
     * 完成工作单元：保存变更、触发完成处理器、然后通知监听器。只能调用一次。
     *
     * @return 当工作单元完成后完成的阶段
     * @throws IllegalStateException 如果已完成请求已发出
     */
    public CompletionStage<Void> completeAsync() {
        if (rolledBack) {
            return CompletableFuture.completedFuture(null);
        }

        if (completed || completing) {
            throw new IllegalStateException("Completion has already been requested for this unit of work.");
        }

        completing = true;
        CompletableFuture<Void> result = new CompletableFuture<>();
        saveChangesAsync().whenComplete((ignored, throwable) -> {
            try {
                if (throwable != null) {
                    failure = unwrap(throwable);
                    result.completeExceptionally(failure);
                    return;
                }

                completed = true;
                invokeCompletedHandlers().whenComplete((unused, handlerFailure) -> {
                    if (handlerFailure != null) {
                        failure = unwrap(handlerFailure);
                        result.completeExceptionally(failure);
                        return;
                    }

                    notifyCompleted();
                    result.complete(null);
                });
            } finally {
                completing = false;
            }
        });

        return result;
    }

    /**
     * 设置外部（父级）工作单元以支持嵌套。
     *
     * @param outer 外部工作单元
     */
    public void setOuter(UnitOfWork outer) {
        this.outer = outer;
    }

    /**
     * 按键查找已注册的上下文。
     *
     * @param key 上下文键
     * @return 上下文，如果未找到则返回 {@code null}
     */
    public UnitOfWorkContext findContext(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key");
        }

        return contexts.get(key);
    }

    /**
     * 在给定键下注册上下文。如果该键下已有上下文，则操作失败。
     *
     * @param key     上下文键
     * @param context 要注册的上下文
     * @throws IllegalStateException 如果该键下已有上下文
     */
    public void addContext(String key, UnitOfWorkContext context) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key");
        }

        if (context == null) {
            throw new IllegalArgumentException("context");
        }

        UnitOfWorkContext previous = contexts.putIfAbsent(key, context);
        if (previous != null) {
            throw new IllegalStateException("This unit of work already contains a context with the key: " + key);
        }
    }

    /**
     * 获取已有上下文，或使用提供的工厂创建新的上下文。
     *
     * @param key     上下文键
     * @param factory 当上下文不存在时用于创建上下文的工厂
     * @return 已有或新创建的上下文
     */
    public UnitOfWorkContext getOrAddContext(String key, Supplier<UnitOfWorkContext> factory) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key");
        }

        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }

        return contexts.computeIfAbsent(key, ignored -> factory.get());
    }

    /**
     * 释放工作单元。关闭所有上下文，如果单元未成功完成则触发失败监听器，
     * 并触发释放监听器。幂等 —— 后续调用为空操作。
     */
    @Override
    public void close() {
        if (disposed) {
            return;
        }

        disposed = true;

        for (UnitOfWorkContext context : contexts.values()) {
            context.close();
        }

        if (!completed || failure != null) {
            notifyFailed();
        }

        notifyDisposed();
    }

    /**
     * 使用给定事件通知所有已注册的完成监听器。
     *
     * @param event 完成事件
     */
    protected void notifyCompleted(UnitOfWorkEvent event) {
        for (Consumer<UnitOfWorkEvent> listener : completedListeners) {
            listener.accept(event);
        }
    }

    /**
     * 使用给定事件通知所有已注册的失败监听器。
     *
     * @param event 失败事件
     */
    protected void notifyFailed(UnitOfWorkFailure event) {
        for (Consumer<UnitOfWorkFailure> listener : failedListeners) {
            listener.accept(event);
        }
    }

    /**
     * 使用给定事件通知所有已注册的释放监听器。
     *
     * @param event 释放事件
     */
    protected void notifyDisposed(UnitOfWorkEvent event) {
        for (Consumer<UnitOfWorkEvent> listener : disposedListeners) {
            listener.accept(event);
        }
    }

    private CompletionStage<Void> invokeCompletedHandlers() {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (Supplier<CompletionStage<Void>> handler : completedHandlers) {
            chain = chain.thenCompose(ignored -> handler.get());
        }

        return chain;
    }

    private void notifyCompleted() {
        notifyCompleted(new UnitOfWorkEvent(this));
    }

    private void notifyFailed() {
        notifyFailed(new UnitOfWorkFailure(this, failure, rolledBack));
    }

    private void notifyDisposed() {
        notifyDisposed(new UnitOfWorkEvent(this));
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof java.util.concurrent.CompletionException completionException
                && completionException.getCause() != null) {
            return completionException.getCause();
        }

        return throwable;
    }
}
