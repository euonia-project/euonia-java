package com.euonia.usecase;

/**
 * 表示应用中的一个用例。用例是可以在系统内执行的特定操作或动作，
 * 定义了执行特定业务逻辑或功能的契约。
 *
 * @param <I> 用例输入的类型
 * @param <O> 用例输出的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCase<I, O> {
    /**
     * 使用给定的输入执行用例并返回输出。
     *
     * @param input 用例的输入数据
     * @return 用例的输出数据
     */
    O execute(I input);
}
