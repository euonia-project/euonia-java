package com.euonia.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用对象池实现，管理类型为 T 的可重用对象池。
 * 该池使用 {@link ObjectPoolPolicy} 来定义对象的创建、验证和销毁行为。
 * 对象在首次获取时惰性创建 —— 当空闲池为空且使用中的对象数低于容量时，才会创建新对象。
 * <p>
 * 当池耗尽时，支持多种超限行为：
 * <ul>
 *   <li>{@code THROW_EXCEPTION} —— 抛出 RuntimeException</li>
 *   <li>{@code RETURN_NULL} —— 返回 null</li>
 *   <li>{@code CREATE_NEW} / {@code AUTO_ADJUST} —— 在容量之外创建新实例</li>
 *   <li>{@code WAIT_FOR_AVAILABLE} —— 阻塞调用线程直到有对象被释放</li>
 * </ul>
 *
 * @param <T> 池管理的对象类型
 * @author <a href="mailto:zhaorong@outlook.com>">damon(zhaorong@outlook.com)</a>
 */
public final class DefaultObjectPool<T> implements ObjectPool<T> {

    /**
     * 最大允许容量，防止无限制扩容。
     */
    private static final int MAX_CAPACITY = 1 << 20; // 1,048,576
    private final int capacity;
    private final Queue<T> idlePool;
    private final ObjectPoolPolicy<T> policy;
    private final AtomicInteger inUseCount = new AtomicInteger(0);

    /**
     * 使用给定的策略和容量构造一个 {@link DefaultObjectPool}。
     *
     * @param policy   定义创建/验证/销毁/超限行为的策略
     * @param capacity 触发超限行为之前允许的最大使用中对象数
     */
    public DefaultObjectPool(ObjectPoolPolicy<T> policy, int capacity) {
        this.policy = policy;
        if (capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Capacity exceeds maximum allowed: " + MAX_CAPACITY);
        }
        this.capacity = capacity;
        this.idlePool = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * 使用给定的策略和默认容量 {@code Runtime.getRuntime().availableProcessors() * 2}
     * 构造一个 {@link DefaultObjectPool}。
     *
     * @param policy 定义创建/验证/销毁/超限行为的策略
     */
    public DefaultObjectPool(ObjectPoolPolicy<T> policy) {
        this(policy, Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * 返回此池的最大容量 —— 触发超限行为时使用中的对象数量阈值。
     *
     * @return 池容量
     */
    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * 返回池中当前空闲（可用）对象的数量。
     *
     * @return 空闲对象计数
     */
    public int getIdleCount() {
        return idlePool.size();
    }

    /**
     * 返回当前正在使用中（已获取但尚未释放）的对象数量。
     *
     * @return 使用中对象计数
     */
    public int getInUseCount() {
        return inUseCount.get();
    }

    /**
     * 返回此池使用的策略。
     *
     * @return 对象池策略
     */
    @Override
    public ObjectPoolPolicy<T> getPolicy() {
        return policy;
    }

    /**
     * 将对象释放回池中。
     * <p>
     * 如果空闲池已满，该对象将通过策略被销毁。
     * 使用中计数器始终会递减。
     * 如果有任何线程处于 {@code WAIT_FOR_AVAILABLE} 模式等待，其中一个将被通知。
     *
     * @param obj 要释放的对象；如果为 {@code null}，此方法为空操作
     */
    @Override
    public synchronized void release(T obj) {
        if (obj == null) {
            return;
        }

        // 始终递减使用中计数器 —— 修复池满时的计数器泄漏
        inUseCount.decrementAndGet();

        if (!idlePool.offer(obj)) {
            policy.destroy(obj);
        }

        // 唤醒一个等待线程（用于 WAIT_FOR_AVAILABLE 行为）
        // 使用 notify() 而非 notifyAll() 可避免惊群问题
        notify();
    }

    /**
     * 从池中获取一个对象。
     * <p>
     * <b>正常路径</b>（inUseCount &lt; capacity）：
     * <ol>
     *   <li>如果空闲池中有可用对象，则对其进行验证并返回。
     *       如果验证失败，对象将被销毁并重试获取。</li>
     *   <li>如果空闲池为空，则通过策略创建新对象。</li>
     * </ol>
     * <p>
     * <b>超限路径</b>（inUseCount &ge; capacity）：
     * <ul>
     *   <li>{@code THROW_EXCEPTION} —— 抛出 {@code RuntimeException}</li>
     *   <li>{@code RETURN_NULL} —— 返回 {@code null}</li>
     *   <li>{@code CREATE_NEW} / {@code AUTO_ADJUST} —— 在容量之外创建新实例</li>
     *   <li>{@code WAIT_FOR_AVAILABLE} —— 阻塞直到有对象被释放，然后重试</li>
     * </ul>
     *
     * @return 获取到的对象，如果超限行为为 {@code RETURN_NULL} 则返回 {@code null}
     * @throws RuntimeException 如果超限行为为 {@code THROW_EXCEPTION}
     */
    @Override
    public synchronized T acquire() {
        while (true) {
            // 步骤 1：尝试从池中获取空闲对象
            T instance = idlePool.poll();
            if (instance != null) {
                if (policy.validate(instance)) {
                    inUseCount.incrementAndGet();
                    return instance;
                }
                // 验证失败 —— 销毁无效对象并重试
                policy.destroy(instance);
                continue;
            }

            // 步骤 2：空闲池为空 —— 检查是否低于容量
            if (inUseCount.get() < capacity) {
                // 低于容量：惰性创建新对象
                instance = policy.create();
                inUseCount.incrementAndGet();
                return instance;
            }

            // 步骤 3：达到或超过容量 —— 应用超限行为
            switch (policy.oversizeBehavior()) {
                case THROW_EXCEPTION:
                    throw new RuntimeException("All pooled objects are in use (capacity=" + capacity + ").");

                case RETURN_NULL:
                    return null;

                case CREATE_NEW:
                    instance = policy.create();
                    inUseCount.incrementAndGet();
                    return instance;

                case WAIT_FOR_AVAILABLE:
                    try {
                        // wait() 会原子性地释放监视器锁并挂起线程。
                        // 这避免了在持有 synchronized 锁的同时调用 acquire() 导致的死锁。
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted while waiting for an available object.", e);
                    }
                    // 被 release() 通知后，循环回去重试获取
                    break;

                default:
                    throw new IllegalStateException("Unexpected oversize behavior: " + policy.oversizeBehavior());
            }
        }
    }
}
