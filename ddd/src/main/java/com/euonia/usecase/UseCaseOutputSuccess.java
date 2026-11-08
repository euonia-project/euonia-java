package com.euonia.usecase;

/**
 * 表示用例输出为成功时的结构。
 *
 * @param <O> 输出结果的类型
 * @author damon(zhaorong@outlook.com)
 */
public interface UseCaseOutputSuccess<O> {

    /**
     * 以成功结果完成用例。
     *
     * @param output 成功输出的结果
     */
    void ok(O output);
}
