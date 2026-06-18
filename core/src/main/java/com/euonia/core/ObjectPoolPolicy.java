package com.euonia.core;

/**
 * ObjectPoolPolicy 是一个定义对象池行为的接口。它指定了对象池如何创建、验证和销毁对象，以及当所有对象都在使用中时池是否应该中断。
 *
 * @param <T> 池中对象的类型
 * @author <a href="mailto:zhaorong@outlook.com>">damon(zhaorong@outlook.com)</a>
 */
public interface ObjectPoolPolicy<T> {

    /**
     * 创建对象的方法。当池需要一个新对象时调用。
     *
     * @return 创建的新对象
     */
    T create();

    /**
     * 验证对象的方法。当对象被从池中获取时调用，以确保它仍然有效。
     *
     * @param obj 要验证的对象
     * @return true 如果对象有效且可以被重用，否则返回 false
     */
    boolean validate(T obj);

    /**
     * 销毁对象的方法。当对象被从池中移除时调用，以执行任何必要的清理操作。
     *
     * @param obj 要销毁的对象
     */
    void destroy(T obj);

    /**
     * 定义当对象池中的对象都在使用中时的行为。这个方法指示池是否应该中断等待线程，或者采取其他措施来处理这种情况。
     *
     * @return 返回超过最大容量时的行为，指示池应该如何处理所有对象都在使用中的情况。
     */
    OversizeBehavior oversizeBehavior();

    /**
     * 定义当对象池中的对象超过预设的最大容量时的行为。
     */
    enum OversizeBehavior {
        /**
         * 当对象池中的对象超过预设的最大容量时，抛出异常。
         */
        THROW_EXCEPTION,
        /**
         * 当对象池中的对象超过预设的最大容量时，返回 null。
         */
        RETURN_NULL,
        /**
         * 当对象池中的对象超过预设的最大容量时，记录警告但继续运行。
         */
        CREATE_NEW,
        /**
         * 当对象池中的对象超过预设的最大容量时，等待直到有可用对象。
         */
        WAIT_FOR_AVAILABLE
    }
}
