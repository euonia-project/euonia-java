package com.euonia.security;

import java.security.Principal;
import java.util.Objects;

/**
 * 表示一个用户声明（Claim），由类型和值组成。
 * <p>
 * 该类实现了 {@link Principal} 接口，可以作为主体添加到
 * {@link javax.security.auth.Subject} 中。
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UserClaim implements Principal {

    /**
     * 声明类型，如 {@link UserClaimTypes#SUBJECT}、{@link UserClaimTypes#ROLE} 等。
     */
    private final String type;

    /**
     * 声明值。
     */
    private final String value;

    /**
     * 使用指定的类型和值构造声明。
     *
     * @param type  声明类型，不能为 null
     * @param value 声明值，可以为 null
     */
    public UserClaim(String type, String value) {
        this.type = Objects.requireNonNull(type, "Claim type must not be null");
        this.value = value;
    }

    /**
     * 获取声明类型。
     *
     * @return 声明类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取声明值。
     *
     * @return 声明值，可能为 null
     */
    public String getValue() {
        return value;
    }

    /**
     * 返回声明类型作为主体名称。
     *
     * @return 声明类型
     */
    @Override
    public String getName() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserClaim)) {
            return false;
        }
        UserClaim userClaim = (UserClaim) o;
        return type.equals(userClaim.type) && Objects.equals(value, userClaim.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }
}
