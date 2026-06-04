package com.euonia.reflection;

/**
 * MissingMethodException is a custom exception that is thrown when a method
 * with a specific name or annotation is not found in a given class.
 * It extends RuntimeException and provides additional information about the
 * type and method that were not found.
 * The exception message includes the type name and the method name or
 * annotation that was expected but not found.
 */
public class MissingMethodException extends RuntimeException {

    private final String typeName;
    private final String methodName;

    /**
     * Constructs a new MissingMethodException with the specified type name and
     * method name.
     *
     * @param typeName   The name of the class where the method was expected to be
     *                   found.
     * @param methodName The name of the method or annotation that was expected but
     *                   not found.
     */
    public MissingMethodException(String typeName, String methodName) {
        super("No method named " + methodName + " or annotated with convention found in " + typeName);
        this.typeName = typeName;
        this.methodName = methodName;
    }

    /**
     * Returns the name of the class where the method was expected to be found.
     *
     * @return The name of the class.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns the name of the method or annotation that was expected but not found.
     *
     * @return The name of the method or annotation.
     */
    public String getMethodName() {
        return methodName;
    }

}
