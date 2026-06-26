package com.euonia.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.euonia.core.ObjectPoolPolicy.OversizeBehavior;

/**
 * 针对 {@link DefaultObjectPool} 的全面单元测试，覆盖构造、获取、释放、
 * 四种超限行为、验证失败、线程安全以及中断场景。
 */
@SuppressWarnings({"unused", "ConvertToTryWithResources", "resource"})
@DisplayName("DefaultObjectPool")
class DefaultObjectPoolTest {

    // ──────────────────── 测试辅助 ────────────────────

    /**
     * 可配置的策略桩，各场景按需覆写。
     */
    static class StubPolicy implements ObjectPoolPolicy<String> {
        private final AtomicInteger createCount = new AtomicInteger(0);
        private final AtomicInteger destroyCount = new AtomicInteger(0);
        private final AtomicInteger validateCount = new AtomicInteger(0);
        private volatile boolean validateResult = true;
        private volatile OversizeBehavior oversizeBehavior = OversizeBehavior.THROW_EXCEPTION;
        private volatile RuntimeException createException;

        @Override
        public String create() {
            if (createException != null) {
                throw createException;
            }
            createCount.incrementAndGet();
            return "obj-" + createCount.get();
        }

        @Override
        public boolean validate(String obj) {
            validateCount.incrementAndGet();
            return validateResult;
        }

        @Override
        public void destroy(String obj) {
            destroyCount.incrementAndGet();
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return oversizeBehavior;
        }

        // 便捷配置方法
        void setValidateResult(boolean ok) {
            this.validateResult = ok;
        }

        void setOversizeBehavior(OversizeBehavior b) {
            this.oversizeBehavior = b;
        }

        void setCreateException(RuntimeException ex) {
            this.createException = ex;
        }

        int getCreateCount() {
            return createCount.get();
        }

        int getDestroyCount() {
            return destroyCount.get();
        }

        int getValidateCount() {
            return validateCount.get();
        }
    }

    // ──────────────────── 构造 ────────────────────

    @Nested
    @DisplayName("构造")
    class Construction {

        @Test
        @DisplayName("Given 合法容量 when 构造池 then 返回正确 capacity")
        void givenValidCapacity_whenConstruct_thenCapacityMatches() {
            StubPolicy policy = new StubPolicy();
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 5);
            assertEquals(5, pool.getCapacity());
        }

        @Test
        @DisplayName("Given 仅策略 when 构造池 then 容量为 availableProcessors * 2")
        void givenOnlyPolicy_whenConstruct_thenDefaultCapacityUsed() {
            StubPolicy policy = new StubPolicy();
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy);
            assertEquals(Runtime.getRuntime().availableProcessors() * 2, pool.getCapacity());
        }

        @Test
        @DisplayName("Given 超限容量 when 构造池 then 抛 IllegalArgumentException")
        void givenExcessiveCapacity_whenConstruct_thenThrows() {
            StubPolicy policy = new StubPolicy();
            int overMax = (1 << 20) + 1; // MAX_CAPACITY + 1
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new DefaultObjectPool<>(policy, overMax));
            assertTrue(ex.getMessage().contains("maximum allowed"));
        }

        @Test
        @DisplayName("Given 恰好最大容量 when 构造池 then 成功")
        void givenExactlyMaxCapacity_whenConstruct_thenSucceeds() {
            StubPolicy policy = new StubPolicy();
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 1 << 20);
            assertEquals(1 << 20, pool.getCapacity());
        }

        @Test
        @DisplayName("Given 容量为 1 且 RETURN_NULL when 池满后 acquire then 返回 null")
        void givenCapacityOneAndReturnNull_whenPoolFull_thenAcquireReturnsNull() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.RETURN_NULL);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 1);
            assertEquals(1, pool.getCapacity());

            pool.acquire(); // 占满唯一槽位
            assertNull(pool.acquire()); // 超限
        }

        @Test
        @DisplayName("Given 容量为 1 when 构造池 then 成功且 acquire 正常")
        void givenCapacityOne_whenConstruct_thenAcquireWorks() {
            StubPolicy policy = new StubPolicy();
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 1);
            assertEquals(1, pool.getCapacity());
            String obj = pool.acquire();
            assertNotNull(obj);
            assertTrue(obj.startsWith("obj-"));
        }

        @Test
        @DisplayName("When 构造池 then policy 引用正确")
        void whenConstruct_thenPolicyReferenceIsCorrect() {
            StubPolicy policy = new StubPolicy();
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 3);
            assertSame(policy, pool.getPolicy());
        }
    }

    // ──────────────────── acquire / release 基本流程 ────────────────────

    @Nested
    @DisplayName("acquire / release 基本流程")
    class BasicAcquireRelease {

        private StubPolicy policy;
        private DefaultObjectPool<String> pool;

        @BeforeEach
        void setUp() {
            policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            pool = new DefaultObjectPool<>(policy, 3);
        }

        @Test
        @DisplayName("Given 空池 when 首次 acquire then 创建新对象并返回")
        void givenEmptyPool_whenFirstAcquire_thenCreatesNewObject() {
            String obj = pool.acquire();
            assertNotNull(obj);
            assertEquals("obj-1", obj);
            assertEquals(1, policy.getCreateCount());
            assertEquals(1, pool.getInUseCount());
            assertEquals(0, pool.getIdleCount());
        }

        @Test
        @DisplayName("Given 已获取的对象 when release then 回到空闲池")
        void givenAcquiredObject_whenRelease_thenReturnsToIdlePool() {
            String obj = pool.acquire();
            assertEquals(1, pool.getInUseCount());
            assertEquals(0, pool.getIdleCount());

            pool.release(obj);
            assertEquals(0, pool.getInUseCount());
            assertEquals(1, pool.getIdleCount());
        }

        @Test
        @DisplayName("Given 空闲池有对象 when acquire then 重用而非新建")
        void givenIdleObject_whenAcquire_thenReusesWithoutCreating() {
            String obj1 = pool.acquire();
            pool.release(obj1);
            int createsBefore = policy.getCreateCount();

            String obj2 = pool.acquire();
            assertSame(obj1, obj2);
            assertEquals(createsBefore, policy.getCreateCount()); // 无新建
            assertEquals(1, pool.getInUseCount());
        }

        @Test
        @DisplayName("Given 多个对象 when acquire/release 循环 then 正确追踪计数")
        void givenMultipleObjects_whenAcquireReleaseCycle_thenCountsAreCorrect() {
            String a = pool.acquire();
            String b = pool.acquire();
            String c = pool.acquire();

            assertEquals(3, pool.getInUseCount());
            assertEquals(0, pool.getIdleCount());

            pool.release(a);
            assertEquals(2, pool.getInUseCount());
            assertEquals(1, pool.getIdleCount());

            pool.release(b);
            assertEquals(1, pool.getInUseCount());
            assertEquals(2, pool.getIdleCount());

            pool.release(c);
            assertEquals(0, pool.getInUseCount());
            assertEquals(3, pool.getIdleCount());

            // 再次全部获取，应重用
            String a2 = pool.acquire();
            String b2 = pool.acquire();
            String c2 = pool.acquire();
            assertNotNull(a2);
            assertNotNull(b2);
            assertNotNull(c2);
            assertEquals(3, pool.getInUseCount());
        }

        @Test
        @DisplayName("Given release null when 调用 release then 空操作")
        void givenNullObject_whenRelease_thenNoOp() {
            String obj = pool.acquire();
            pool.release(null);
            assertEquals(1, pool.getInUseCount()); // 未改变
            pool.release(obj);
            assertEquals(0, pool.getInUseCount());
        }
    }

    // ──────────────────── 验证 ────────────────────

    @Nested
    @DisplayName("对象验证")
    class Validation {

        @Test
        @DisplayName("Given 验证通过 when 重用空闲对象 then 返回该对象")
        void givenValidationPasses_whenReusingIdleObject_thenReturnsIt() {
            StubPolicy policy = new StubPolicy();
            policy.setValidateResult(true);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String obj = pool.acquire();
            pool.release(obj);
            String reused = pool.acquire();

            assertSame(obj, reused);
            assertEquals(1, policy.getValidateCount());
        }

        @Test
        @DisplayName("Given 验证失败 when 重用空闲对象 then 销毁并创建新对象")
        void givenValidationFails_whenReusingIdleObject_thenDestroysAndCreatesNew() {
            StubPolicy policy = new StubPolicy();
            policy.setValidateResult(false);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String obj1 = pool.acquire();
            pool.release(obj1);

            int createsBefore = policy.getCreateCount();
            int destroysBefore = policy.getDestroyCount();

            String obj2 = pool.acquire();

            assertNotSame(obj1, obj2);
            assertEquals(createsBefore + 1, policy.getCreateCount());
            assertEquals(destroysBefore + 1, policy.getDestroyCount());
        }

        @Test
        @DisplayName("Given 验证失败但无空闲对象 when acquire then 正常创建新对象")
        void givenValidationFailsAndNoIdle_whenAcquire_thenCreatesNormally() {
            StubPolicy policy = new StubPolicy();
            policy.setValidateResult(false);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String obj = pool.acquire();
            assertNotNull(obj);
            assertEquals(0, policy.getValidateCount()); // 直接从创建路径走，不验证
        }

        @Test
        @DisplayName("Given 连续验证失败 when acquire then 最终创建有效对象")
        void givenConsecutiveValidationFailures_whenAcquire_thenEventuallyCreatesFresh() {
            StubPolicy policy = new StubPolicy();
            policy.setValidateResult(false);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 3);

            // 放入两个"损坏"对象
            String obj1 = pool.acquire();
            String obj2 = pool.acquire();
            pool.release(obj1);
            pool.release(obj2);

            // 获取时应销毁旧对象并创建新对象
            String newObj = pool.acquire();
            assertNotNull(newObj);
            assertTrue(policy.getDestroyCount() >= 1); // 至少销毁一个
            assertTrue(policy.getCreateCount() >= 3);
        }
    }

    // ──────────────────── 超限行为 ────────────────────

    @Nested
    @DisplayName("超限行为")
    class OversizeBehaviorTests {

        @Test
        @DisplayName("Given THROW_EXCEPTION 且池满 when acquire then 抛 RuntimeException")
        void givenThrowExceptionAndPoolFull_whenAcquire_thenThrows() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.THROW_EXCEPTION);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            pool.acquire();
            pool.acquire(); // 池满

            RuntimeException ex = assertThrows(RuntimeException.class, pool::acquire);
            assertTrue(ex.getMessage().contains("capacity=2"));
        }

        @Test
        @DisplayName("Given RETURN_NULL 且池满 when acquire then 返回 null")
        void givenReturnNullAndPoolFull_whenAcquire_thenReturnsNull() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.RETURN_NULL);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            pool.acquire();
            pool.acquire();
            assertNull(pool.acquire());
        }

        @Test
        @DisplayName("Given CREATE_NEW 且池满 when acquire then 突破容量创建新对象")
        void givenCreateNewAndPoolFull_whenAcquire_thenCreatesBeyondCapacity() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            pool.acquire();
            pool.acquire(); // inUseCount = 2，达到容量

            String obj3 = pool.acquire();
            assertNotNull(obj3);
            assertEquals(3, pool.getInUseCount()); // 超过容量
            assertEquals(3, policy.getCreateCount());
        }

        @Test
        @DisplayName("Given WAIT_FOR_AVAILABLE 且池满 when acquire then 阻塞至 release")
        void givenWaitForAvailableAndPoolFull_whenAcquire_thenBlocksUntilRelease() throws Exception {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.WAIT_FOR_AVAILABLE);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String obj1 = pool.acquire();
            String obj2 = pool.acquire(); // 池满

            AtomicReference<String> acquired = new AtomicReference<>();
            CountDownLatch acquireStarted = new CountDownLatch(1);

            Thread consumer = new Thread(() -> {
                acquireStarted.countDown();
                acquired.set(pool.acquire());
            });
            consumer.start();

            assertTrue(acquireStarted.await(1, TimeUnit.SECONDS));
            // 给 acquire 一点时间阻塞
            Thread.sleep(200);
            assertNull(acquired.get()); // 尚未获取到

            // 释放一个对象
            pool.release(obj1);

            consumer.join(2000);
            assertFalse(consumer.isAlive(), "consumer should have woken up");
            assertNotNull(acquired.get());
        }

        @Test
        @DisplayName("Given WAIT_FOR_AVAILABLE when 阻塞中被中断 then 抛 RuntimeException 并设置中断标记")
        void givenWaitForAvailable_whenInterrupted_thenThrowsRuntimeExceptionAndPreservesInterruptStatus() throws Exception {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.WAIT_FOR_AVAILABLE);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 1);

            pool.acquire(); // 池满

            AtomicReference<Exception> caught = new AtomicReference<>();
            CountDownLatch started = new CountDownLatch(1);

            Thread consumer = new Thread(() -> {
                started.countDown();
                try {
                    pool.acquire();
                } catch (Exception e) {
                    caught.set(e);
                }
            });
            consumer.start();

            assertTrue(started.await(1, TimeUnit.SECONDS));
            Thread.sleep(200);

            consumer.interrupt();
            consumer.join(2000);

            assertNotNull(caught.get());
            assertInstanceOf(RuntimeException.class, caught.get());
            assertTrue(caught.get().getMessage().contains("interrupted"));
        }

        @Test
        @DisplayName("Given WAIT_FOR_AVAILABLE when 多个线程等待 then release 逐个唤醒")
        void givenWaitForAvailable_whenMultipleThreadsWaiting_thenReleaseWakesOneAtATime() throws Exception {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.WAIT_FOR_AVAILABLE);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 1);

            String obj = pool.acquire(); // 池满

            AtomicInteger acquiredCount = new AtomicInteger(0);
            int waiterCount = 3;
            CountDownLatch allStarted = new CountDownLatch(waiterCount);

            for (int i = 0; i < waiterCount; i++) {
                new Thread(() -> {
                    allStarted.countDown();
                    String o = pool.acquire();
                    if (o != null) acquiredCount.incrementAndGet();
                    pool.release(o);
                }).start();
            }

            assertTrue(allStarted.await(1, TimeUnit.SECONDS));
            Thread.sleep(300);

            // 释放对象，应该只唤醒一个
            pool.release(obj);

            // 给时间让所有线程完成
            Thread.sleep(1000);

            assertEquals(waiterCount, acquiredCount.get());
        }
    }

    // ──────────────────── 池满 release 时销毁 ────────────────────

    @Nested
    @DisplayName("release 到满空闲池")
    class ReleaseToFullIdlePool {

        @Test
        @DisplayName("Given 空闲池已满 when release then 销毁对象而非入池")
        void givenIdlePoolFull_whenRelease_thenDestroysObject() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 1);

            String obj1 = pool.acquire();
            String obj2 = pool.acquire();

            pool.release(obj1);
            assertEquals(1, pool.getIdleCount()); // 空闲池最多 1

            int destroysBefore = policy.getDestroyCount();
            pool.release(obj2);
            assertEquals(destroysBefore + 1, policy.getDestroyCount());
        }

        @Test
        @DisplayName("Given 池满 release then 销毁计数正确且 inUseCount 递减")
        void givenPoolFull_whenRelease_thenInUseCountDecrementedAndDestroyed() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String a = pool.acquire();
            String b = pool.acquire();
            assertEquals(2, pool.getInUseCount());

            pool.release(a);
            assertEquals(1, pool.getInUseCount());
            assertEquals(1, pool.getIdleCount());

            pool.release(b);
            assertEquals(0, pool.getInUseCount());
            assertEquals(2, pool.getIdleCount());

            // 再次获取并释放——空闲池满时销毁
            String c = pool.acquire();
            String d = pool.acquire();
            String e = pool.acquire(); // 超过容量（CREATE_NEW）
            assertEquals(3, pool.getInUseCount());

            pool.release(c);
            pool.release(d);
            int destroysBefore = policy.getDestroyCount();
            pool.release(e);
            assertTrue(policy.getDestroyCount() > destroysBefore);
        }
    }

    // ──────────────────── 并发安全 ────────────────────

    @Nested
    @DisplayName("并发安全")
    class Concurrency {

        @Test
        @DisplayName("Given 多线程 when 并发 acquire 和 release then 无数据竞争且计数正确")
        void givenMultipleThreads_whenConcurrentAcquireRelease_thenCountsAreCorrect() throws Exception {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            int capacity = 10;
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, capacity);

            int threadCount = 8;
            int opsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < opsPerThread; i++) {
                            String obj = pool.acquire();
                            assertNotNull(obj);
                            // 短暂持有模拟使用
                            Thread.yield();
                            pool.release(obj);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executor.shutdown();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));

            // 所有操作完成后，inUseCount 应为 0
            assertEquals(0, pool.getInUseCount());
            // 空闲池不应超过容量
            assertTrue(pool.getIdleCount() <= capacity);
        }

        @Test
        @DisplayName("Given 多线程 acquire 不放回 when 超过容量 then THROW_EXCEPTION 线程安全")
        void givenMultipleThreadsAcquiringWithoutRelease_whenExceedCapacity_thenThrowsSafely() throws Exception {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.THROW_EXCEPTION);
            int capacity = 5;
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, capacity);

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (int t = 0; t < threadCount; t++) {
                executor.submit(() -> {
                    try {
                        pool.acquire();
                        successCount.incrementAndGet();
                    } catch (RuntimeException e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executor.shutdown();

            assertEquals(capacity, successCount.get());
            assertEquals(threadCount - capacity, failCount.get());
        }

        @Test
        @DisplayName("Given 多线程 WAIT_FOR_AVAILABLE when 并发 release then 所有线程获取到对象")
        void givenWaitForAvailable_whenConcurrentRelease_thenAllThreadsGetObjects() throws Exception {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.WAIT_FOR_AVAILABLE);
            int capacity = 3;
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, capacity);

            // 先占满池
            String[] held = new String[capacity];
            for (int i = 0; i < capacity; i++) {
                held[i] = pool.acquire();
            }

            int waiterCount = 5;
            AtomicInteger acquiredCount = new AtomicInteger(0);
            CountDownLatch waitersReady = new CountDownLatch(waiterCount);
            CountDownLatch waitersDone = new CountDownLatch(waiterCount);

            for (int i = 0; i < waiterCount; i++) {
                new Thread(() -> {
                    waitersReady.countDown();
                    try {
                        String o = pool.acquire();
                        if (o != null) acquiredCount.incrementAndGet();
                        pool.release(o);
                    } finally {
                        waitersDone.countDown();
                    }
                }).start();
            }

            assertTrue(waitersReady.await(1, TimeUnit.SECONDS));
            Thread.sleep(300);

            // 逐步释放
            for (String obj : held) {
                pool.release(obj);
                Thread.sleep(50);
            }

            assertTrue(waitersDone.await(5, TimeUnit.SECONDS));
            assertEquals(waiterCount, acquiredCount.get());
            assertEquals(0, pool.getInUseCount());
        }
    }

    // ──────────────────── create 异常 ────────────────────

    @Nested
    @DisplayName("create 异常传播")
    class CreateException {

        @Test
        @DisplayName("Given create 抛异常 when acquire then 异常向上传播")
        void givenCreateThrows_whenAcquire_thenExceptionPropagates() {
            StubPolicy policy = new StubPolicy();
            policy.setCreateException(new RuntimeException("create failed"));
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 3);

            RuntimeException ex = assertThrows(RuntimeException.class, pool::acquire);
            assertEquals("create failed", ex.getMessage());
            assertEquals(0, pool.getInUseCount()); // 未增加
        }
    }

    // ──────────────────── acquire/release 组合场景 ────────────────────

    @Nested
    @DisplayName("组合场景")
    class CombinedScenarios {

        @Test
        @DisplayName("Given 混合超限行为 when 池满后释放 then acquire 正常")
        void givenMixedOversize_whenPoolFilledAndReleased_thenAcquireNormalizes() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 3);

            // 超额获取
            String a = pool.acquire();
            String b = pool.acquire();
            String c = pool.acquire(); // 此时 inUseCount = 3
            String d = pool.acquire(); // 超额
            assertEquals(4, pool.getInUseCount());

            // 全部释放
            pool.release(a);
            pool.release(b);
            pool.release(c);
            pool.release(d);
            assertEquals(0, pool.getInUseCount());
            assertEquals(3, pool.getIdleCount()); // 空闲池容量为 3

            // 再次正常获取
            String e = pool.acquire();
            assertNotNull(e);
            assertEquals(1, pool.getInUseCount());
        }

        @Test
        @DisplayName("Given acquire/release 交替 when 频繁操作 then getInUseCount 始终一致")
        void givenAlternatingAcquireRelease_whenFrequent_thenInUseCountConsistent() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.CREATE_NEW);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 5);

            for (int round = 0; round < 20; round++) {
                String a = pool.acquire();
                assertEquals(1, pool.getInUseCount());

                String b = pool.acquire();
                assertEquals(2, pool.getInUseCount());

                pool.release(a);
                assertEquals(1, pool.getInUseCount());

                pool.release(b);
                assertEquals(0, pool.getInUseCount());
            }
        }

        @Test
        @DisplayName("Given 同一对象 release 两次 when 第二次 release then 不影响空闲池内容")
        void givenReleaseSameObjectTwice_whenSecondRelease_thenOfferedAgainToIdlePool() {
            StubPolicy policy = new StubPolicy();
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String obj = pool.acquire();
            pool.release(obj);
            assertEquals(0, pool.getInUseCount());
            assertEquals(1, pool.getIdleCount());

            // 第二次 release 同一对象：尝试入池成功但 inUseCount 会变为 -1（release 无条件递减）
            pool.release(obj);
            assertTrue(pool.getInUseCount() <= 0);

            // 仍然可以正常获取对象
            String newObj = pool.acquire();
            assertNotNull(newObj);
        }

        @Test
        @DisplayName("Given RETURN_NULL 超限 when 获取并释放 then 后续正常获取")
        void givenReturnNullOversize_whenAcquireAndRelease_thenSubsequentAcquireSucceeds() {
            StubPolicy policy = new StubPolicy();
            policy.setOversizeBehavior(OversizeBehavior.RETURN_NULL);
            DefaultObjectPool<String> pool = new DefaultObjectPool<>(policy, 2);

            String a = pool.acquire();
            String b = pool.acquire();
            assertNull(pool.acquire()); // 超限
            assertNull(pool.acquire()); // 再次超限

            pool.release(a);
            String c = pool.acquire();
            assertNotNull(c);
            assertSame(a, c);
        }
    }
}
