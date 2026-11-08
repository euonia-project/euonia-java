package com.euonia.usecase;

/**
 * 表示用例输出为失败时的结构。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCaseOutputFailure {

    /**
     * 报告用例执行失败。
     *
     * @param throwable 导致用例失败的异常
     */
    void error(Throwable throwable);
}
