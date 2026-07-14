package com.euonia.usecase;

/**
 * 表示用例执行的失败输出。该接口定义了一个方法来处理用例执行过程中发生的错误。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCaseFailure {
    /**
     * 表示用例执行因错误而失败。
     *
     * @param throwable 导致失败的异常
     */
    void error(Throwable throwable);
}
