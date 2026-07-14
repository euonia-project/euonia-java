package com.euonia.usecase;

/**
 * 表示用例执行的成功输出。该接口定义了一个方法来处理用例执行成功的结果。
 *
 * @param <O> 成功输出的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCaseSuccess<O> {
    /**
     * 表示用例执行已成功并返回结果。
     *
     * @param output 成功执行的结果
     */
    void success(O output);
}
