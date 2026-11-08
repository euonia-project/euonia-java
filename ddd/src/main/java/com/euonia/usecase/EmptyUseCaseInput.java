package com.euonia.usecase;

/**
 * 表示无输入的用例。
 *
 * @author damon(zhaorong@outlook.com)
 */
public record EmptyUseCaseInput() implements UseCaseInput {

    /**
     * 无输入用例的共享实例。
     */
    public static final EmptyUseCaseInput INSTANCE = new EmptyUseCaseInput();
}
