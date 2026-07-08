package com.euonia.annotation;

import java.lang.annotation.Annotation;

/**
 * Validator 接口定义了一个通用的验证器，用于对注解和对应的值进行验证。实现该接口的类需要提供具体的验证逻辑。
 *
 * @param <A> 注解类型，必须是 Annotation 的子类
 */
public interface Validator<A extends Annotation> {
    /**
     * 验证给定的注解和对应的值是否符合验证规则。
     *
     * @param annotation 要验证的注解实例
     * @param value      要验证的值
     * @return 一个 Result 对象，其中 result 表示验证结果（true 表示验证通过，false 表示验证失败），message 为错误消息（如果验证失败，则包含错误消息，否则为 null）
     */
    Result validate(A annotation, Object value);

    /**
     * Result 是一个不可变的记录类，用于表示验证结果。它包含两个字段：result 和 message。
     * <ul>
     *     <li>result：一个布尔值，表示验证是否通过（true）或失败（false）。</li>
     *     <li>message：一个字符串，表示验证失败时的错误消息，如果验证通过则为 null。</li>
     * </ul>
     *
     * @param result  验证结果（true 表示验证通过，false 表示验证失败）
     * @param message 验证失败时的错误消息，如果验证通过则为 null
     */
    record Result(boolean result, String message) {
    }
}
