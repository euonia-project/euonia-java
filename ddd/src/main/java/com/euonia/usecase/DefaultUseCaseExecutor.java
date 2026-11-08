package com.euonia.usecase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.euonia.reflection.ServiceProvider;

/**
 * {@link UseCaseExecutor} 的默认实现。
 * <p>
 * 统一封装「执行用例 + 结果分发」过程：
 * <ul>
 *   <li>用例正常返回时调用 {@code presenter.ok(output)}；</li>
 *   <li>用例抛出异常时调用 {@code presenter.error(throwable)}，由呈现器决定取消语义；</li>
 *   <li>无输出/无输入/无参用例通过 {@link EmptyUseCaseOutput#INSTANCE} 与
 *       {@link EmptyUseCaseInput#INSTANCE} 适配到统一执行路径；</li>
 *   <li>容器解析重载从 {@link ServiceProvider} 解析用例实例后走同一执行路径。</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultUseCaseExecutor implements UseCaseExecutor {

    private final ServiceProvider serviceProvider;

    /**
     * 使用指定的服务提供者构造执行器。
     *
     * @param serviceProvider 用于解析用例实例的服务提供者
     */
    public DefaultUseCaseExecutor(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public <I, O> CompletionStage<Void> executeAsync(UseCase<I, O> useCase, I input, UseCasePresenter<O> presenter) {
        return executeCore(() -> useCase.executeAsync(input), presenter);
    }

    @Override
    public <I> CompletionStage<Void> executeAsync(NonOutputUseCase<I> useCase, I input, UseCasePresenter<EmptyUseCaseOutput> presenter) {
        return executeCore(() -> useCase.executeVoidAsync(input)
                                       .thenApply(ignored -> EmptyUseCaseOutput.INSTANCE), presenter);
    }

    @Override
    public <O> CompletionStage<Void> executeAsync(NonInputUseCase<O> useCase, UseCasePresenter<O> presenter) {
        return executeCore(useCase::executeAsync, presenter);
    }

    @Override
    public CompletionStage<Void> executeAsync(ParameterlessUseCase useCase, UseCasePresenter<EmptyUseCaseOutput> presenter) {
        return executeCore(() -> useCase.executeVoidAsync()
                                       .thenApply(ignored -> EmptyUseCaseOutput.INSTANCE), presenter);
    }

    @Override
    public <C extends UseCase<?, O>, O> CompletionStage<Void> executeAsync(Class<C> useCaseType, Object input, UseCasePresenter<O> presenter) {
        return executeCore(() -> {
            var useCase = serviceProvider.getRequiredService(useCaseType);
            @SuppressWarnings("unchecked")
            var output = (CompletionStage<O>) useCase.executeUntypedAsync(input);
            return output;
        }, presenter);
    }

    @Override
    public <C extends NonOutputUseCase<?>> CompletionStage<Void> executeNonOutputAsync(Class<C> useCaseType, Object input, UseCasePresenter<EmptyUseCaseOutput> presenter) {
        return executeCore(() -> {
            var useCase = serviceProvider.getRequiredService(useCaseType);
            @SuppressWarnings("unchecked")
            var typed = (NonOutputUseCase<Object>) useCase;
            return typed.executeVoidAsync(input).thenApply(ignored -> EmptyUseCaseOutput.INSTANCE);
        }, presenter);
    }

    @Override
    public <C extends NonInputUseCase<O>, O> CompletionStage<Void> executeNonInputAsync(Class<C> useCaseType, UseCasePresenter<O> presenter) {
        return executeCore(() -> serviceProvider.getRequiredService(useCaseType).executeAsync(), presenter);
    }

    @Override
    public <C extends ParameterlessUseCase> CompletionStage<Void> executeParameterlessAsync(Class<C> useCaseType, UseCasePresenter<EmptyUseCaseOutput> presenter) {
        return executeCore(() -> serviceProvider.getRequiredService(useCaseType)
                                                .executeVoidAsync()
                                                .thenApply(ignored -> EmptyUseCaseOutput.INSTANCE), presenter);
    }

    private static <O> CompletionStage<Void> executeCore(Supplier<CompletionStage<O>> executor, UseCasePresenter<O> presenter) {
        try {
            var stage = executor.get();
            if (stage == null) {
                presenter.error(new IllegalStateException("Use case returned a null CompletionStage."));
                return CompletableFuture.completedFuture(null);
            }

            return stage.handle((output, throwable) -> {
                if (throwable == null) {
                    presenter.ok(output);
                } else {
                    presenter.error(unwrap(throwable));
                }
                return null;
            });
        } catch (Throwable throwable) {
            presenter.error(throwable);
            return CompletableFuture.completedFuture(null);
        }
    }

    private static Throwable unwrap(Throwable throwable) {
        var current = throwable;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
            && current.getCause() != null
            && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
