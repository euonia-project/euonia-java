package com.euonia.usecase;

/**
 * 表示不应返回任何业务输出的用例。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class EmptyUseCaseOutput implements UseCaseOutput {

    /**
     * 无输出用例的共享实例。
     */
    public static final EmptyUseCaseOutput INSTANCE = new EmptyUseCaseOutput();

    private EmptyUseCaseOutput() {
    }
}
