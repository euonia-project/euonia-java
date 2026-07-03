package com.euonia.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 对象反射操作工具类，提供安全的方法查找和反射调用功能。
 * <p>
 * 所有方法都通过 {@link Optional} 优雅地处理方法不存在的情况，避免直接抛出异常。
 * 该类为抽象工具类，不可实例化。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class ObjectUtility {
    /**
     * 在指定的类中查找公共方法（包括继承的方法）。
     *
     * @param clazz          要查找的类
     * @param methodName     方法名
     * @param parameterTypes 方法参数类型
     * @return 包含找到的方法的 {@link Optional}，如果未找到则为空
     */
    public static Optional<Method> getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return Optional.of(clazz.getMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * 在指定的类中查找已声明的方法（仅包括该类自身声明的方法，不包括继承的）。
     *
     * @param clazz          要查找的类
     * @param methodName     方法名
     * @param parameterTypes 方法参数类型
     * @return 包含找到的方法的 {@link Optional}，如果未找到则为空
     */
    public static Optional<Method> getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return Optional.of(clazz.getDeclaredMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * 通过反射在指定对象上调用方法，参数类型从参数实例中自动推断。
     * <p>
     * 如果方法未找到，返回 {@code null}；如果调用过程中发生异常，返回 {@code null}。
     *
     * @param obj        要调用方法的对象
     * @param methodName 方法名
     * @param args       方法参数
     * @return 方法调用的返回值，如果方法不存在或调用失败则返回 {@code null}
     */
    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        Class<?> clazz = obj.getClass();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return getMethod(clazz, methodName, parameterTypes).map(method -> {
            try {
                return method.invoke(obj, args);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                return null;
            }
        }).orElse(null);
    }

    /**
     * 通过反射在指定对象上调用方法，并将返回值强制转换为指定类型。
     * <p>
     * 参数类型从参数实例中自动推断。如果方法未找到或调用失败，返回 {@code null}。
     *
     * @param <T>        期望的返回类型
     * @param returnType 返回类型的 {@link Class}
     * @param obj        要调用方法的对象
     * @param methodName 方法名
     * @param args       方法参数
     * @return 强制转换为指定类型的方法返回值，如果方法不存在或调用失败则返回 {@code null}
     */
    public static <T> T invokeMethod(Class<T> returnType, Object obj, String methodName, Object... args) {
        Class<?> clazz = obj.getClass();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return getMethod(clazz, methodName, parameterTypes).map(method -> {
            try {
                return returnType.cast(method.invoke(obj, args));
            } catch (IllegalAccessException | InvocationTargetException exception) {
                return null;
            }
        }).orElse(null);
    }
}
