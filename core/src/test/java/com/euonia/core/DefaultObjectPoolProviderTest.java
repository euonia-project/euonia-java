package com.euonia.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 针对 {@link DefaultObjectPoolProvider} 的单元测试，覆盖单例、缓存、
 * 创建/移除池以及线程安全等场景。
 * <p>
 * 注意：Provider 以 {@code policy.getClass()} 为缓存键，
 * 因此每次测试使用独立的具名内部类以避免跨测试缓存的污染。
 */
@DisplayName("DefaultObjectPoolProvider")
class DefaultObjectPoolProviderTest {

    @Nested
    @DisplayName("单例")
    class Singleton {

        @Test
        @DisplayName("When 多次调用 getInstance then 返回同一实例")
        void whenGetInstanceCalledMultipleTimes_thenSameInstanceReturned() {
            DefaultObjectPoolProvider p1 = DefaultObjectPoolProvider.getInstance();
            DefaultObjectPoolProvider p2 = DefaultObjectPoolProvider.getInstance();
            assertSame(p1, p2);
        }
    }

    @Nested
    @DisplayName("create(policy)")
    class CreateWithPolicy {

        @Test
        @DisplayName("Given 新策略 when create then 返回默认容量的池且 policy 引用正确")
        void givenNewPolicy_whenCreate_thenReturnsPoolWithDefaultCapacity() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            PolicyA policy = new PolicyA();

            ObjectPool<String> pool = provider.create(policy);
            assertNotNull(pool);
            assertEquals(Runtime.getRuntime().availableProcessors() * 2, pool.getCapacity());
            assertSame(policy, pool.getPolicy());
        }

        @Test
        @DisplayName("Given 同一策略类型 when 多次 create then 返回缓存实例")
        void givenSamePolicyType_whenCreateAgain_thenReturnsCachedInstance() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            PolicyB policy = new PolicyB();

            ObjectPool<String> pool1 = provider.create(policy);
            ObjectPool<String> pool2 = provider.create(policy);

            assertSame(pool1, pool2);
        }

        @Test
        @DisplayName("Given 同一策略类不同实例 when create then 返回缓存池且 policy 为首实例")
        void givenSamePolicyClassDifferentInstances_whenCreate_thenFirstPolicyWins() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();

            PolicyC first = new PolicyC();
            ObjectPool<String> pool1 = provider.create(first);

            PolicyC second = new PolicyC();
            ObjectPool<String> pool2 = provider.create(second);

            assertSame(pool1, pool2);
            assertSame(first, pool2.getPolicy());
        }

        @Test
        @DisplayName("Given 不同策略类型 when create then 返回不同池实例")
        void givenDifferentPolicyTypes_whenCreate_thenDifferentPools() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();

            ObjectPool<String> pool1 = provider.create(new PolicyD());
            ObjectPool<String> pool2 = provider.create(new PolicyE());

            assertNotSame(pool1, pool2);
        }
    }

    @Nested
    @DisplayName("create(policy, size)")
    class CreateWithPolicyAndSize {

        @Test
        @DisplayName("Given 新策略和指定容量 when create then 返回指定容量的池")
        void givenNewPolicyAndSize_whenCreate_thenReturnsPoolWithSpecifiedCapacity() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            PolicyF policy = new PolicyF();

            ObjectPool<String> pool = provider.create(policy, 15);
            assertNotNull(pool);
            assertEquals(15, pool.getCapacity());
            assertSame(policy, pool.getPolicy());
        }

        @Test
        @DisplayName("Given 已缓存策略 when 用不同容量 create then 首次容量生效")
        void givenCachedPolicy_whenCreateWithDifferentSize_thenFirstCapacityWins() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();

            PolicyG first = new PolicyG();
            ObjectPool<String> pool1 = provider.create(first, 20);

            PolicyG second = new PolicyG();
            ObjectPool<String> pool2 = provider.create(second, 30);

            assertSame(pool1, pool2);
            assertEquals(20, pool2.getCapacity());
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        @DisplayName("Given 已创建的池 when remove then 后续 create 返回新实例")
        void givenExistingPool_whenRemove_thenNextCreateReturnsNewInstance() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            PolicyH policy = new PolicyH();

            ObjectPool<String> pool1 = provider.create(policy);
            provider.remove(policy);

            ObjectPool<String> pool2 = provider.create(policy);
            assertNotSame(pool1, pool2);
        }

        @Test
        @DisplayName("Given 未创建的策略 when remove then 无异常")
        void givenNonExistentPolicy_whenRemove_thenNoException() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            assertDoesNotThrow(() -> provider.remove(new PolicyI()));
        }

        @Test
        @DisplayName("Given remove 后 when 再次 remove then 无异常")
        void givenRemovedPolicy_whenRemoveAgain_thenNoException() {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            PolicyJ policy = new PolicyJ();

            provider.create(policy);
            provider.remove(policy);
            assertDoesNotThrow(() -> provider.remove(policy));
        }
    }

    @Nested
    @DisplayName("线程安全")
    class Concurrency {

        @Test
        @DisplayName("Given 多线程 when 并发 create 同一策略实例 then 返回同一池")
        void givenMultipleThreads_whenCreateSamePolicyConcurrently_thenSameInstance() throws Exception {
            DefaultObjectPoolProvider provider = DefaultObjectPoolProvider.getInstance();
            PolicyK policy = new PolicyK();
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            ObjectPool<?>[] results = new ObjectPool<?>[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    results[idx] = provider.create(policy);
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join(2000);
            }

            ObjectPool<?> first = results[0];
            for (int i = 1; i < threadCount; i++) {
                assertSame(first, results[i], "Thread " + i + " got a different instance");
            }
        }
    }

    // ──────────────────── 策略实现（每个测试用独立类以避免缓存键冲突） ────────────────────

    abstract static class BasePolicy implements ObjectPoolPolicy<String> {
        @Override
        public boolean validate(String obj) {
            return true;
        }

        @Override
        public void destroy(String obj) {
        }
    }

    static class PolicyA extends BasePolicy {
        @Override
        public String create() {
            return "A";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyB extends BasePolicy {
        @Override
        public String create() {
            return "B";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyC extends BasePolicy {
        @Override
        public String create() {
            return "C";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyD extends BasePolicy {
        @Override
        public String create() {
            return "D";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyE extends BasePolicy {
        @Override
        public String create() {
            return "E";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyF extends BasePolicy {
        @Override
        public String create() {
            return "F";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyG extends BasePolicy {
        @Override
        public String create() {
            return "G";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyH extends BasePolicy {
        @Override
        public String create() {
            return "H";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyI extends BasePolicy {
        @Override
        public String create() {
            return "I";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyJ extends BasePolicy {
        @Override
        public String create() {
            return "J";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }

    static class PolicyK extends BasePolicy {
        @Override
        public String create() {
            return "K";
        }

        @Override
        public OversizeBehavior oversizeBehavior() {
            return OversizeBehavior.CREATE_NEW;
        }
    }
}
