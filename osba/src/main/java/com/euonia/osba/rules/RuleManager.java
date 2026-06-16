package com.euonia.osba.rules;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理与特定类型关联的规则，提供获取和管理该类型规则的方法。
 * <p>
 * RuleManager 设计为线程安全，允许并发访问和修改规则而无需外部同步。
 * 内部使用 {@link ConcurrentMap} 存储不同类型对应的 RuleManager 实例，
 * 使用 {@link CopyOnWriteArrayList} 存储每个类型的规则列表，确保修改规则时不影响并发读取。
 *
 * @author damon(zhaorong@outlook)
 */
public final class RuleManager {

    /**
     * 按类型存储 RuleManager 实例的线程安全映射。
     */
    private static final ConcurrentMap<Class<?>, RuleManager> ruleSets = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 当前类型对应的规则列表，使用写时复制策略保证线程安全。
     */
    private final List<Rule> rules = new CopyOnWriteArrayList<>();

    /**
     * 私有构造函数，防止外部直接实例化。
     */
    private RuleManager() {
    }

    /**
     * 获取当前类型对应的规则列表。
     *
     * @return 规则列表
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * 获取与指定类型关联的 RuleManager 实例。若该类型尚无对应的 RuleManager，则创建新实例并存入映射。
     *
     * @param type 要获取其 RuleManager 的类类型
     * @return 与指定类型关联的 RuleManager 实例
     */
    public static RuleManager getRules(Class<?> type) {
        return ruleSets.computeIfAbsent(type, c -> new RuleManager());
    }

    /**
     * 移除与指定类型关联的 RuleManager，相当于清除该类型的所有规则。
     *
     * @param type 要清除规则的类类型
     */
    public static void cleanRules(Class<?> type) {
        synchronized (ruleSets) {
            ruleSets.remove(type);
        }
    }

    /**
     * 标记该 RuleManager 是否已完成初始化（已为其关联类型添加了规则）。
     */
    private boolean initialized = false;

    /**
     * 判断当前 RuleManager 是否已完成初始化。
     * 此属性可用于确定规则是否已设置完毕并准备就绪。
     *
     * @return 若已初始化则返回 {@code true}，否则返回 {@code false}
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 设置当前 RuleManager 的初始化状态。
     * 可在为关联类型添加完规则后调用此方法将 RuleManager 标记为已初始化。
     *
     * @param initialized {@code true} 表示已初始化，{@code false} 表示未初始化
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
