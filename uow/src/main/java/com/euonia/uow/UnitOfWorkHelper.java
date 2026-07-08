package com.euonia.uow;

import java.lang.reflect.Method;

import com.euonia.uow.annotation.UnitOfWork;

/**
 * 用于检查类型和方法上工作单元注解和标记接口的静态工具方法。
 *
 * <p>主要由 AOP 拦截器和代理使用，用于判断给定的类或方法
 * 是否应被包装在工作单元中。</p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class UnitOfWorkHelper {
    private UnitOfWorkHelper() {
    }

    /**
     * 如果类型具有工作单元注解（类或方法级别）或实现了 {@link UnitOfWorkEnabled}，
     * 则返回 {@code true}。
     *
     * @param implementationType 要检查的类型
     * @return 如果类型需要工作单元则返回 {@code true}
     */
    public static boolean isUnitOfWorkType(Class<?> implementationType) {
        if (implementationType == null) {
            throw new IllegalArgumentException("implementationType");
        }

        if (hasUnitOfWorkAnnotation(implementationType) || anyMethodHasUnitOfWorkAnnotation(implementationType)) {
            return true;
        }

        return UnitOfWorkEnabled.class.isAssignableFrom(implementationType);
    }

    /**
     * 如果方法具有活跃的（未禁用的）{@link UnitOfWork} 注解，则返回 {@code true}。
     *
     * @param method 要检查的方法
     * @return 如果方法标注了 {@code @UnitOfWork} 则返回 {@code true}
     */
    public static boolean isUnitOfWorkMethod(Method method) {
        return getUnitOfWorkAnnotation(method) != null;
    }

    /**
     * 从方法或其声明类中检索活跃的 {@link UnitOfWork} 注解。
     * 如果注解不存在或 {@link UnitOfWork#disabled() disabled}，则返回 {@code null}。
     *
     * @param method 要检查的方法
     * @return 注解，或 {@code null}
     */
    public static UnitOfWork getUnitOfWorkAnnotation(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("method");
        }

        UnitOfWork annotation = method.getAnnotation(UnitOfWork.class);
        if (annotation != null) {
            return annotation.disabled() ? null : annotation;
        }

        Class<?> declaringClass = method.getDeclaringClass();
        annotation = declaringClass.getAnnotation(UnitOfWork.class);
        if (annotation != null) {
            return annotation.disabled() ? null : annotation;
        }

        return null;
    }

    private static boolean anyMethodHasUnitOfWorkAnnotation(Class<?> implementationType) {
        for (Method method : implementationType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(UnitOfWork.class)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasUnitOfWorkAnnotation(Class<?> implementationType) {
        UnitOfWork annotation = implementationType.getAnnotation(UnitOfWork.class);
        return annotation != null && !annotation.disabled();
    }
}
