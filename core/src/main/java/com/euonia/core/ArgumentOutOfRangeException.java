package com.euonia.core;

import com.euonia.utility.Resource;

import java.util.function.Supplier;

public class ArgumentOutOfRangeException extends IllegalArgumentException {
    public ArgumentOutOfRangeException(String message) {
        super(message);
    }

    public static <T extends Comparable<T>> void throwIfEquals(T argument, T expected, Supplier<String> message) {
        if (argument.compareTo(expected) != 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfGreaterThan(T argument, T target, Supplier<String> message) {
        if (argument.compareTo(target) <= 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfGreaterThanOrEqual(T argument, T target, Supplier<String> message) {
        if (argument.compareTo(target) < 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfLessThan(T argument, T target, Supplier<String> message) {
        if (argument.compareTo(target) >= 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfLessThanOrEqual(T argument, T target, Supplier<String> message) {
        if (argument.compareTo(target) > 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfNotInRange(T argument, T min, T max, Supplier<String> message) {
        if (argument.compareTo(min) >= 0 && argument.compareTo(max) <= 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfInRange(T argument, T min, T max, Supplier<String> message) {
        if (argument.compareTo(min) < 0 || argument.compareTo(max) > 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static <T extends Comparable<T>> void throwIfNotInRange(T argument, Range<T> range, Supplier<String> message) {
        throwIfNotInRange(argument, range.min(), range.max(), message);
    }

    public static <T extends Comparable<T>> void throwIfInRange(T argument, Range<T> range, Supplier<String> message) {
        throwIfInRange(argument, range.min(), range.max(), message);
    }

    public static void throwIfNegative(Number argument, Supplier<String> message) {
        if (argument.doubleValue() >= 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static void throwIfNegativeOrZero(Number argument, Supplier<String> message) {
        if (argument.doubleValue() > 0) {
            return;
        }

        throw new ArgumentOutOfRangeException(message.get());
    }

    public static void throwIfZero(Number argument, Supplier<String> message) {
        if (argument.doubleValue() == 0) {
            throw new ArgumentOutOfRangeException(message.get());
        }
    }

    public static <T extends Comparable<T>> void throwIfEquals(T argument, T target, String param) {
        throwIfEquals(argument, target, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.Equals", param, target));
    }

    public static <T extends Comparable<T>> void throwIfGreaterThan(T argument, T target, String param) {
        throwIfGreaterThan(argument, target, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.GreaterThan", param, target));
    }

    public static <T extends Comparable<T>> void throwIfGreaterThanOrEqual(T argument, T target, String param) {
        throwIfGreaterThanOrEqual(argument, target, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.GreaterThanOrEqual", param, target));
    }

    public static <T extends Comparable<T>> void throwIfLessThan(T argument, T target, String param) {
        throwIfLessThan(argument, target, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.LessThan", param, target));
    }

    public static <T extends Comparable<T>> void throwIfLessThanOrEqual(T argument, T target, String param) {
        throwIfLessThanOrEqual(argument, target, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.LessThanOrEqual", param, target));
    }

    public static <T extends Comparable<T>> void throwIfNotInRange(T argument, T min, T max, String param) {
        throwIfNotInRange(argument, min, max, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.NotInRange", param, min, max));
    }

    public static <T extends Comparable<T>> void throwIfInRange(T argument, T min, T max, String param) {
        throwIfInRange(argument, min, max, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.InRange", param, min, max));
    }

    public static <T extends Comparable<T>> void throwIfNotInRange(T argument, Range<T> range, String param) {
        throwIfNotInRange(argument, range, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.NotInRange", param, range.min(), range.max()));
    }

    public static <T extends Comparable<T>> void throwIfInRange(T argument, Range<T> range, String param) {
        throwIfInRange(argument, range, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.InRange", param, range.min(), range.max()));
    }

    public static void throwIfNegative(Number argument, String param) {
        throwIfNegative(argument, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.Negative", param));
    }

    public static void throwIfNegativeOrZero(Number argument, String param) {
        throwIfNegativeOrZero(argument, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.NegativeOrZero", param));
    }

    public static void throwIfZero(Number argument, String param) {
        throwIfZero(argument, () -> Resource.getString("core", "ArgumentOutOfRangeException.Message.Zero", param));
    }
}
