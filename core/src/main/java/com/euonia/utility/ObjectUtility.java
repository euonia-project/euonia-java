package com.euonia.utility;

import java.lang.reflect.Method;
import java.util.Optional;

public abstract class ObjectUtility {
    public static Optional<Method> getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return Optional.of(clazz.getMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static Optional<Method> getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return Optional.of(clazz.getDeclaredMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        Class<?> clazz = obj.getClass();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return getMethod(clazz, methodName, parameterTypes).map(method -> {
            try {
                return method.invoke(obj, args);
            } catch (Exception e) {
                return null;
            }
        }).orElse(null);
    }

    public static <T> T invokeMethod(Class<T> returnType, Object obj, String methodName, Object... args) {
        Class<?> clazz = obj.getClass();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return getMethod(clazz, methodName, parameterTypes).map(method -> {
            try {
                return returnType.cast(method.invoke(obj, args));
            } catch (Exception e) {
                return null;
            }
        }).orElse(null);
    }
}
