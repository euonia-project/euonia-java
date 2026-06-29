package com.euonia.utility;

import java.util.List;
import java.util.function.Predicate;

public class Assert {
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object obj, java.util.function.Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new IllegalArgumentException(messageSupplier.get());
        }
    }

    public static void notEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty() || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(List<?> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void notContains(List<T> list, Predicate<T> predicate, String message) {
        if (list != null && list.stream().anyMatch(predicate)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void notContains(T[] array, Predicate<T> predicate, String message) {
        if (array != null && java.util.Arrays.stream(array).anyMatch(predicate)) {
            throw new IllegalArgumentException(message);
        }
    }
}
