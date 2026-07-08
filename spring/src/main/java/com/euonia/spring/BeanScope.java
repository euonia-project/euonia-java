package com.euonia.spring;

/**
 * 定义了 Spring IoC 容器中 Bean 的作用域常量。
 * <p>
 * 这些作用域可以在 Spring 配置中定义 Bean 时用于指定 Bean 的作用域。
 * <p>
 * 定义的常量包括：
 * <ul>
 *     <li>{@link #APPLICATION} —— 将单个 Bean 定义的作用域限定为 ServletContext 的生命周期。仅在 Web 感知的 Spring ApplicationContext 中有效。</li>
 *     <li>{@link #PROTOTYPE} —— 将单个 Bean 定义的作用域限定为任意数量的对象实例。</li>
 *     <li>{@link #REQUEST} —— 将单个 Bean 定义的作用域限定为单个 HTTP 请求的生命周期。仅在 Web 感知的 Spring ApplicationContext 中有效。</li>
 *     <li>{@link #SESSION} —— 将单个 Bean 定义的作用域限定为 HTTP Session 的生命周期。仅在 Web 感知的 Spring ApplicationContext 中有效。</li>
 *     <li>{@link #SINGLETON} —— 将单个 Bean 定义的作用域限定为每个 Spring IoC 容器的单个对象实例。</li>
 *     <li>{@link #WEB_SOCKET} —— 将单个 Bean 定义的作用域限定为 WebSocket 的生命周期。仅在 Web 感知的 Spring ApplicationContext 中有效。</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class BeanScope {
    /**
     * 将单个 Bean 定义的作用域限定为 ServletContext 的生命周期。
     * 仅在 Web 感知的 Spring ApplicationContext 中有效。
     */
    public final static String APPLICATION = "application";

    /**
     * 将单个 Bean 定义的作用域限定为任意数量的对象实例。
     */
    public final static String PROTOTYPE = "prototype";

    /**
     * 将单个 Bean 定义的作用域限定为单个 HTTP 请求的生命周期。
     * 即每个 HTTP 请求都有自己基于单个 Bean 定义创建的 Bean 实例。
     * 仅在 Web 感知的 Spring ApplicationContext 中有效。
     */
    public final static String REQUEST = "request";

    /**
     * 将单个 Bean 定义的作用域限定为 HTTP Session 的生命周期。
     * 仅在 Web 感知的 Spring ApplicationContext 中有效。
     */
    public final static String SESSION = "session";

    /**
     * 将单个 Bean 定义的作用域限定为每个 Spring IoC 容器的单个对象实例。
     */
    public final static String SINGLETON = "singleton";

    /**
     * 将单个 Bean 定义的作用域限定为 WebSocket 的生命周期。
     * 仅在 Web 感知的 Spring ApplicationContext 中有效。
     */
    public final static String WEB_SOCKET = "websocket";

    /*
    APPLICATION("application"),
    PROTOTYPE("prototype"),
    REQUEST("request"),
    SESSION("session"),
    SINGLETON("singleton"),
    WEB_SOCKET("websocket");

    private final String value;

    BeanScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    */
}
