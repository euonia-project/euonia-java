package com.euonia.bus.convention;

import com.euonia.core.ArgumentNullException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 内置的消息约定，聚合多个 {@link MessageConvention} 实例，并使用 {@link ConcurrentHashMap} 缓存类型分类结果。
 * <p>
 * 每种消息类型（unicast、multicast、request）都有自己专用的缓存，由 {@code ConcurrentHashMap<Class<?>, Boolean>} 支持。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class BaseMessageConvention implements MessageConvention {

    /**
     * 默认约定名称
     */
    private static final String DEFAULT_NAME = "Default";

    /**
     * 默认可覆盖的消息约定实例
     */
    private final OverridableMessageConvention defaultConvention = new OverridableMessageConvention(new DefaultMessageConvention());

    /**
     * 已注册的消息约定列表
     */
    private final List<MessageConvention> conventions = new ArrayList<>();

    /**
     * 单播消息类型判断的缓存
     */
    private final ConventionCache unicastConventionCache = new ConventionCache();

    /**
     * 组播消息类型判断的缓存
     */
    private final ConventionCache multicastConventionCache = new ConventionCache();

    /**
     * 请求消息类型判断的缓存
     */
    private final ConventionCache requestConventionCache = new ConventionCache();

    /**
     * 构造方法，初始化时注册默认可覆盖的约定。
     */
    public BaseMessageConvention() {
        conventions.add(defaultConvention);
    }

    /**
     * 判断指定的类型是否为单播消息类型。
     *
     * @param channel 待检查的类型，不能为 {@code null}
     * @return 若任一已注册的约定将该类型归类为单播，返回 {@code true}
     * @throws NullPointerException 若 {@code channel} 为 {@code null}
     */
    @Override
    public boolean isUnicast(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        return unicastConventionCache.apply(channel, type -> conventions.stream().anyMatch(c -> c.isUnicast(type)));
    }

    /**
     * 判断指定的类型是否为组播消息类型。
     *
     * @param channel 待检查的类型，不能为 {@code null}
     * @return 若任一已注册的约定将该类型归类为组播，返回 {@code true}
     * @throws NullPointerException 若 {@code channel} 为 {@code null}
     */
    @Override
    public boolean isMulticast(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");

        return multicastConventionCache.apply(channel, type -> conventions.stream().anyMatch(c -> c.isMulticast(type)));
    }

    /**
     * 判断指定的类型是否为请求消息类型。
     *
     * @param channel 待检查的类型，不能为 {@code null}
     * @return 若任一已注册的约定将该类型归类为请求，返回 {@code true}
     * @throws NullPointerException 若 {@code channel} 为 {@code null}
     */
    @Override
    public boolean isRequest(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");

        return requestConventionCache.apply(channel, type -> conventions.stream().anyMatch(c -> c.isRequest(type)));
    }

    /**
     * 获取此约定的名称。
     *
     * @return 约定的名称，始终为 "Default"
     */
    @Override
    public String getName() {
        return DEFAULT_NAME;
    }

    // ---- 约定定义方法（包级别访问，供配置使用） ----

    /**
     * 定义单播类型的判断条件。
     *
     * @param convention 判断指定类型是否为单播的谓词
     */
    public void defineUnicastTypeConvention(Predicate<String> convention) {
        ArgumentNullException.throwIfNull(convention, "convention");
        defaultConvention.setUnicastPredicate(convention);
    }

    /**
     * 定义组播类型的判断条件。
     *
     * @param convention 判断指定类型是否为组播的谓词
     */
    public void defineMulticastTypeConvention(Predicate<String> convention) {
        ArgumentNullException.throwIfNull(convention, "convention");
        defaultConvention.setMulticastPredicate(convention);
    }

    /**
     * 定义请求类型的判断条件。
     *
     * @param convention 判断指定类型是否为请求的谓词
     */
    public void defineRequestTypeConvention(Predicate<String> convention) {
        ArgumentNullException.throwIfNull(convention, "convention");
        defaultConvention.setRequestPredicate(convention);
    }

    /**
     * 定义统一的类型约定，将消息类型映射到其 {@link MessageConventionType}，
     * 然后将逻辑拆分为独立的单播、组播和请求谓词。
     *
     * @param convention 将类型分类为约定类别的函数
     */
    public void defineTypeConvention(Function<String, MessageConventionType> convention) {
        ArgumentNullException.throwIfNull(convention, "convention");

        defineUnicastTypeConvention(type -> convention.apply(type) == MessageConventionType.UNICAST);
        defineMulticastTypeConvention(type -> convention.apply(type) == MessageConventionType.MULTICAST);
        defineRequestTypeConvention(type -> convention.apply(type) == MessageConventionType.REQUEST);
    }

    /**
     * 添加一个或多个额外的消息约定。
     *
     * @param conventions 要添加的约定数组，不能为 {@code null} 或空
     * @throws IllegalArgumentException 若 {@code conventions} 为 {@code null} 或空数组
     */
    public void add(MessageConvention... conventions) {
        ArgumentNullException.throwIfNullOrEmpty(conventions, "conventions");
        this.conventions.addAll(Arrays.asList(conventions));
        resetCache(); // 重置缓存以确保新约定生效
    }

    /**
     * 返回所有已注册约定的名称（用于诊断目的）。
     *
     * @return 已注册约定的名称数组
     */
    public String[] getRegisteredConventions() {
        return conventions.stream()
                          .map(MessageConvention::getName)
                          .toArray(String[]::new);
    }

    /**
     * 重置所有缓存的条目，强制在下一次访问时重新计算类型分类结果。
     */
    private void resetCache() {
        unicastConventionCache.reset();
        multicastConventionCache.reset();
        requestConventionCache.reset();
    }

    // ---- 内部缓存类 ----

    /**
     * 线程安全的、以类型为键的缓存，首次访问时计算值，并在后续对同一键的所有查找中重用该值。
     */
    private static class ConventionCache {

        /**
         * 存储缓存结果的并发哈希映射
         */
        private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

        /**
         * 返回给定类型的缓存结果，若首次访问则使用提供的函数计算并存储。
         *
         * @param type       待查找或计算的类型
         * @param convention 缓存未命中时用于计算结果的函数
         * @return 缓存或新计算的布尔值
         */
        boolean apply(String type, Predicate<String> convention) {
            ArgumentNullException.throwIfNullOrEmpty(type, "type");
            ArgumentNullException.throwIfNull(convention, "convention");
            return cache.computeIfAbsent(type, convention::test);
        }

        /**
         * 清除所有缓存的条目。
         */
        void reset() {
            cache.clear();
        }
    }
}
