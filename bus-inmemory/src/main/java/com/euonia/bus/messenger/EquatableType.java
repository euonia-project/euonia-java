package com.euonia.bus.messenger;

import java.util.Objects;

/**
 * 表示消息类型与其令牌（通道）类型键的不可变二元组。用作类型字典中的复合键，以查找特定消息/令牌类型组合的已注册处理器。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class EquatableType {

    private final Class<?> messageType;
    private final Class<?> tokenType;

    /**
     * 使用指定的消息类型和令牌类型构造新实例。
     *
     * @param messageType 消息类型，不能为 {@code null}
     * @param tokenType   令牌（通道）类型，不能为 {@code null}
     */
    public EquatableType(Class<?> messageType, Class<?> tokenType) {
        this.messageType = Objects.requireNonNull(messageType, "messageType must not be null");
        this.tokenType = Objects.requireNonNull(tokenType, "tokenType must not be null");
    }

    /**
     * 获取消息类型。
     *
     * @return 消息类型
     */
    public Class<?> getMessageType() {
        return messageType;
    }

    /**
     * 获取令牌（通道）类型。
     *
     * @return 令牌类型
     */
    public Class<?> getTokenType() {
        return tokenType;
    }

    /**
     * 基于消息类型和令牌类型的引用相等性进行比较。
     *
     * @param obj 要比较的对象
     * @return 如果两个 {@code EquatableType} 的消息类型和令牌类型均相同则返回 {@code true}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof EquatableType other))
            return false;
        return messageType == other.messageType && tokenType == other.tokenType;
    }

    /**
     * 使用 djb2 算法组合消息类型和令牌类型的哈希码。
     *
     * @return 组合后的哈希码
     */
    @Override
    public int hashCode() {
        // djb2 哈希组合
        int hash = messageType.hashCode();
        hash = (hash << 5) + hash;
        hash += tokenType.hashCode();
        return hash;
    }

    /**
     * 返回包含消息类型和令牌类型名称的字符串表示。
     *
     * @return 格式为 "EquatableType{message=..., token=...}" 的字符串
     */
    @Override
    public String toString() {
        return "EquatableType{message=" + messageType.getSimpleName() + ", token=" + tokenType.getSimpleName() + "}";
    }
}
