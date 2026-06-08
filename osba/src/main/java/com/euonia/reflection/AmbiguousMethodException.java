package com.euonia.reflection;

/**
 * AmbiguousMethodException is a custom exception that is thrown when multiple
 * methods match the criteria for a factory method, making it unclear which
 * method should be used.
 * It extends RuntimeException and provides a message that describes the
 * ambiguity in the method selection process.
 * This exception is typically thrown when there are multiple methods with the
 * same name or annotation that could potentially be used as a factory method,
 * and the criteria provided does not allow for a clear distinction between them.
 */
public class AmbiguousMethodException extends RuntimeException {
    public AmbiguousMethodException(String message) {
        super(message);
    }
}
