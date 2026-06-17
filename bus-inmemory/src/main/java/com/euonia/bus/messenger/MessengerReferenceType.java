package com.euonia.bus.messenger;

/**
 * 定义 {@link Messenger} 实现所使用的引用类型。
 * <p>
 * 这是 .NET {@code MessengerReferenceType} 枚举的 Java 等价物。
 *
 * @author damon(zhaorong@outlook)
 */
public enum MessengerReferenceType {

    /**
     * 使用强引用来跟踪已注册的接收者。
     * 接收者必须手动取消注册以避免内存泄漏。
     */
    STRONG,

    /**
     * 使用弱引用来跟踪已注册的接收者。
     * 不再被其他地方引用的接收者将被自动垃圾回收。
     */
    WEAK
}
