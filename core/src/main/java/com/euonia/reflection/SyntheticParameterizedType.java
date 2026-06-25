package com.euonia.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * {@link ParameterizedType} 的可合成实现，允许在运行时手动构造参数化类型。
 * <p>
 * 当无法通过反射直接获取泛型类型信息时（如需要动态组合原始类型和类型参数），可使用该类创建合成的参数化类型实例。
 * <p>
 * 示例用法：
 * <pre>{@code
 * // 构造 List<String> 对应的 ParameterizedType
 * var type = SyntheticParameterizedType.withGenerics(List.class, String.class);
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 */
@SuppressWarnings("NullableProblems")
public final class SyntheticParameterizedType implements ParameterizedType {
    /** 原始类型，如 {@code List.class} */
    private final Type rawType;

    /** 类型参数数组，如 {@code [String.class]} */
    private final Type[] typeArguments;

    /**
     * 使用指定的原始类型和类型参数构造合成参数化类型。
     *
     * @param rawType       原始类型
     * @param typeArguments 类型参数
     */
    public SyntheticParameterizedType(Type rawType, Type... typeArguments) {
        this.rawType = rawType;
        this.typeArguments = typeArguments;
    }

    /**
     * 返回类型的完整名称字符串，包含泛型参数。
     * 例如对于 {@code List<String>} 将返回 {@code "java.util.List<java.lang.String>"}。
     *
     * @return 包含泛型参数的类型名称
     */
    @Override
    public String getTypeName() {
        String typeName = this.rawType.getTypeName();
        if (this.typeArguments.length > 0) {
            StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
            for (Type argument : this.typeArguments) {
                stringJoiner.add(argument.getTypeName());
            }
            return typeName + stringJoiner;
        }
        return typeName;
    }

    /**
     * 返回所有者类型。合成类型永远没有所有者，因此始终返回 null。
     *
     * @return 始终为 {@code null}
     */
    @Override
    public Type getOwnerType() {
        return null;
    }

    /**
     * 返回此参数化类型的原始类型。
     *
     * @return 原始类型
     */
    @Override
    public Type getRawType() {
        return this.rawType;
    }

    /**
     * 返回此参数化类型的实际类型参数数组。
     *
     * @return 类型参数数组
     */
    @Override
    public Type[] getActualTypeArguments() {
        return this.typeArguments;
    }

    /**
     * 基于原始类型和类型参数判断相等性。
     * 当两个参数化类型的原始类型和类型参数分别相等时返回 true。
     *
     * @param other 要比较的对象
     * @return 如果相等则返回 true
     */
    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof ParameterizedType that &&
            that.getOwnerType() == null && this.rawType.equals(that.getRawType()) &&
            Arrays.equals(this.typeArguments, that.getActualTypeArguments())));
    }

    /**
     * 基于原始类型和类型参数计算哈希码。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return (this.rawType.hashCode() * 31 + Arrays.hashCode(this.typeArguments));
    }

    /**
     * 返回类型名称字符串，委托给 {@link #getTypeName()}。
     *
     * @return 类型名称字符串
     */
    @Override
    public String toString() {
        return getTypeName();
    }

    /**
     * 便捷工厂方法，用原始类型和类型参数创建合成参数化类型。
     *
     * @param rawType       原始类型
     * @param typeArguments 类型参数
     * @return 新的 {@link SyntheticParameterizedType} 实例
     */
    public static SyntheticParameterizedType withGenerics(Type rawType, Type... typeArguments) {
        return new SyntheticParameterizedType(rawType, typeArguments);
    }
}
