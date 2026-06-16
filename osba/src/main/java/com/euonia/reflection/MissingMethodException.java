package com.euonia.reflection;

/**
 * MissingMethodException 是一个自定义异常，当在给定类中找不到具有特定名称或注解的方法时抛出。
 * 它继承自 RuntimeException，并提供有关未找到的类型和方法的相关信息。
 * 异常消息包含类型名称以及预期但未找到的方法名称或注解。
 *
 * @author damon(zhaorong@outlook)
 */
public class MissingMethodException extends RuntimeException {

    private final String typeName;
    private final String methodName;

    /**
     * 使用指定的类型名称和方法名称构造一个新的 MissingMethodException。
     *
     * @param typeName   预期找到该方法的类名称。
     * @param methodName 预期但未找到的方法名称或注解。
     */
    public MissingMethodException(String typeName, String methodName) {
        super("No method named " + methodName + " or annotated with convention found in " + typeName);
        this.typeName = typeName;
        this.methodName = methodName;
    }

    /**
     * 返回预期找到该方法的类名称。
     *
     * @return 类名称。
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 返回预期但未找到的方法名称或注解。
     *
     * @return 方法名称或注解。
     */
    public String getMethodName() {
        return methodName;
    }

}
