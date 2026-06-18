package com.euonia.osba;

/**
 * 表示可执行的业务对象。该类继承自 BusinessObject 类，为 execute 和 create 方法提供了默认实现，子类可以重写这些方法以提供特定的行为。
 *
 * @param <T> 业务对象的类型，必须继承自 ExecutableObject
 * @author damon(zhaorong@outlook.com)
 */
public abstract class ExecutableObject<T extends ExecutableObject<T>> extends BusinessObject<T> {

    /**
     * 执行业务对象的操作。默认实现不执行任何操作，子类可以重写此方法以提供特定的行为。
     */
    protected void execute() {
        // 默认实现不执行任何操作，可由子类重写
    }

    /**
     * 创建业务对象的操作。默认实现不执行任何操作，子类可以重写此方法以提供特定的行为。
     */
    protected void create() {
        // 默认实现不执行任何操作，可由子类重写
    }
}
