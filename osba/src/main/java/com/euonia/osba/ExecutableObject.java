package com.euonia.osba;

/**
 * Represents a business object that can be executed. This class extends the BusinessObject class and provides default implementations for the execute and create methods, which can be overridden by subclasses to provide specific behavior.
 *
 * @param <T> the type of the business object, which must extend ExecutableObject
 */
public abstract class ExecutableObject<T extends ExecutableObject<T>> extends BusinessObject<T> {

    protected void execute() {
        // Default implementation does nothing, can be overridden by subclasses
    }

    protected void create() {
        // Default implementation does nothing, can be overridden by subclasses
    }
}
