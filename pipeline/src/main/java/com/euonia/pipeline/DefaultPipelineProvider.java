package com.euonia.pipeline;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.euonia.reflection.ServiceProvider;

/**
 * {@link DefaultPipelineProvider} 是 {@link Pipeline} 的具体实现，使用反射来调用管道行为。
 * 它同时支持同步和异步的 handle 方法，以及实现了 {@link PipelineBehavior} 接口的行为。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class DefaultPipelineProvider<C, R> extends PipelineBase<C, R> {
    /**
     * 同步处理方法名
     */
    private static final String HANDLE_METHOD_NAME = "handle";
    /**
     * 异步处理方法名
     */
    private static final String HANDLE_METHOD_NAME_ASYNC = "handleAsync";

    /**
     * 服务提供者，用于解析和创建行为实例
     */
    private final ServiceProvider provider;

    /**
     * 使用指定的服务提供者构造管道提供者。
     *
     * @param provider 服务提供者
     */
    public DefaultPipelineProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取下一个管道委托。
     * <p>
     * 如果行为类型实现了 {@link PipelineBehavior} 接口，则直接调用其异步处理方法；
     * 否则使用反射查找 handle/handleAsync 方法进行调用。
     *
     * @param next                 链中的下一个委托
     * @param behaviorType         行为类型
     * @param constructorArguments 构造参数，next 委托会被自动添加到参数列表开头
     * @return 包装了行为调用的管道委托
     */
    @Override
    protected PipelineDelegate<C, R> getNext(PipelineDelegate<C, R> next, Class<?> behaviorType, Object... constructorArguments) {
        if (PipelineBehavior.class.isAssignableFrom(behaviorType)) {
            return request -> {
                PipelineBehavior<C, R> behavior = provider.getServiceOrCreate(castType(behaviorType), constructorArguments);
                return behavior.handleAsync(request, next);
            };
        }

        Method method = resolveHandleMethod(behaviorType);
        Object[] ctorArgs = prepend(next, constructorArguments);
        Object instance = provider.createInstance(castType(behaviorType), ctorArgs);

        return request -> invokeTyped(method, instance, request);
    }

    /**
     * 安全地将 {@link Class<?>} 转换为 {@link Class<T>}，避免原始类型警告。
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> castType(Class<?> type) {
        return (Class<T>) type;
    }

    /**
     * 通过反射调用行为方法，自动解析除第一个参数（请求）之外的所有方法参数。
     * <p>
     * 如果方法返回值是 {@link CompletionStage}，则返回异步结果；
     * 否则返回已完成的空 {@link CompletableFuture}。
     *
     * @param method   要调用的方法
     * @param instance 行为实例
     * @param request  请求对象
     * @return 包含方法返回值的完成阶段
     */
    @SuppressWarnings("unchecked")
    private CompletionStage<R> invokeTyped(Method method, Object instance, C request) {
        try {
            Object[] args = new Object[method.getParameterCount()];
            args[0] = request;
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 1; i < parameterTypes.length; i++) {
                args[i] = this.provider.getRequiredService(parameterTypes[i]);
            }
            Object value = method.invoke(instance, args);
            if (value instanceof CompletionStage<?> stage) {
                return (CompletionStage<R>) stage.thenApply(v -> (R) v);
            }
            return CompletableFuture.completedFuture(null);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * 在给定的行为类型中查找 handle 或 handleAsync 方法。
     * <p>
     * 选择标准：
     * <ul>
     *   <li>方法名为 {@code handle} 或 {@code handleAsync}</li>
     *   <li>至少有一个参数</li>
     *   <li>返回类型为 {@link CompletionStage}</li>
     *   <li>如果同时存在两个方法，选取参数个数较少的那个</li>
     * </ul>
     *
     * @param behaviorType 要搜索的行为类型
     * @return 匹配的方法
     * @throws IllegalStateException 如果未找到或找到多个匹配的方法
     */
    private static Method resolveHandleMethod(Class<?> behaviorType) {
        Method[] candidates = Arrays.stream(behaviorType.getMethods())
                                    .filter(m -> HANDLE_METHOD_NAME.equals(m.getName()) || HANDLE_METHOD_NAME_ASYNC.equals(m.getName()))
                                    .filter(m -> m.getParameterCount() >= 1)
                                    .filter(m -> CompletionStage.class.isAssignableFrom(m.getReturnType()))
                                    .sorted(Comparator.comparingInt(Method::getParameterCount))
                                    .toArray(Method[]::new);

        if (candidates.length == 0) {
            throw new IllegalStateException("Method handle/handleAsync not found on " + behaviorType.getName());
        }
        if (candidates.length > 1) {
            throw new IllegalStateException("Multiple handle methods found on " + behaviorType.getName());
        }

        Method method = candidates[0];
        method.setAccessible(true);
        return method;
    }

    /**
     * 将一个元素前置添加到数组的开头。
     *
     * @param first 要放在数组开头的元素
     * @param rest  原始数组
     * @return 包含 first 后跟 rest 所有元素的新数组
     */
    private static Object[] prepend(Object first, Object[] rest) {
        Object[] values = new Object[rest.length + 1];
        values[0] = first;
        System.arraycopy(rest, 0, values, 1, rest.length);
        return values;
    }
}
