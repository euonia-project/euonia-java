package com.euonia.security;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户主体，封装 {@link Subject} 并提供了方便的用户信息访问方法。
 * <p>
 * 这是围绕 {@link Subject} 的轻量包装器，公开了常见的应用程序特定声明值
 *（用户 ID、用户名、编码、租户、角色）以及查询声明和角色的便捷方法。
 * 该包装器不会修改底层的 {@link Subject}。
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UserPrincipal {

    /**
     * 底层安全主题
     */
    private final Subject subject;

    /**
     * 使用指定的 {@link Subject} 构造用户主体。
     *
     * @param subject 安全主题，可以为 null，此时大多数访问器将相应地返回 null 或 false
     */
    public UserPrincipal(Subject subject) {
        this.subject = subject;
    }

    // region 基础属性

    /**
     * 获取底层的 {@link Subject}。
     *
     * @return 安全主题
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * 获取用户名（第一个主体的名称）。
     *
     * @return 用户名，如果不存在则返回 null
     */
    public String getName() {
        if (subject == null) {
            return null;
        }
        return subject.getPrincipals().stream()
                      .filter(Objects::nonNull)
                      .map(Principal::getName)
                      .findFirst()
                      .orElse(null);
    }

    // endregion

    // region 类型化声明访问器

    /**
     * 获取用户标识（Subject ID）。
     * <p>
     * 此方法返回 {@link UserClaimTypes#SUBJECT}（"sub"）声明的值。
     * 如果找不到匹配的声明，则返回 null。
     * </p>
     *
     * @return 用户唯一标识符，如果不存在则返回 null
     */
    public String getUserId() {
        return getClaim(UserClaimTypes.SUBJECT);
    }

    /**
     * 获取用户显示名称。
     *
     * @return {@link UserClaimTypes#NAME} 声明的值，如果声明缺失或底层 {@link Subject} 为 null 则返回 null
     */
    public String getUsername() {
        return getClaim(UserClaimTypes.NAME);
    }

    /**
     * 获取用户编码。
     *
     * @return {@link UserClaimTypes#CODE} 声明的值，如果声明缺失或底层 {@link Subject} 为 null 则返回 null
     */
    public String getCode() {
        return getClaim(UserClaimTypes.CODE);
    }

    /**
     * 获取与用户关联的租户标识。
     *
     * @return {@link UserClaimTypes#TENANT} 声明的值，如果声明或底层 {@link Subject} 不存在则可能为 null
     */
    public String getTenant() {
        return getClaim(UserClaimTypes.TENANT);
    }

    /**
     * 获取用户的角色名称列表。
     * <p>
     * 此方法选择所有类型为 {@link UserClaimTypes#ROLE} 的声明的值。
     * 如果底层 {@link Subject} 为 null，则返回空列表。
     * </p>
     *
     * @return 角色名称列表，可能为空列表
     */
    public List<String> getRoles() {
        return findClaims(UserClaimTypes.ROLE).stream()
                                              .map(UserClaim::getValue)
                                              .collect(Collectors.toList());
    }

    // endregion

    // region 声明查询方法

    /**
     * 获取指定类型的声明（Claim）值。
     *
     * @param claimType 声明类型，参考 {@link UserClaimTypes} 中的常量
     * @return 声明值，如果不存在则返回 null
     */
    public String getClaim(String claimType) {
        UserClaim claim = findClaim(claimType);
        return claim != null ? claim.getValue() : null;
    }

    /**
     * 查找具有指定声明类型的第一个声明。
     *
     * @param claimType 要搜索的声明类型
     * @return 找到的第一个匹配 {@link UserClaim}；如果未找到或 subject 为 null 则返回 null
     */
    public UserClaim findClaim(String claimType) {
        if (subject == null) {
            return null;
        }
        return subject.getPrincipals().stream()
                      .filter(principal -> principal instanceof UserClaim)
                      .map(principal -> (UserClaim) principal)
                      .filter(claim -> claim.getType().equals(claimType))
                      .findFirst()
                      .orElse(null);
    }

    /**
     * 查找与指定声明类型匹配的所有声明。
     *
     * @param claimType 要搜索的声明类型
     * @return 包含所有匹配 {@link UserClaim} 实例的列表。如果没有匹配的声明或 subject 为 null，则返回空列表。
     */
    public List<UserClaim> findClaims(String claimType) {
        if (subject == null) {
            return Collections.emptyList();
        }
        return subject.getPrincipals().stream()
                      .filter(principal -> principal instanceof UserClaim)
                      .map(principal -> (UserClaim) principal)
                      .filter(claim -> claim.getType().equals(claimType))
                      .collect(Collectors.toList());
    }

    /**
     * 返回底层 {@link Subject} 持有的所有声明。
     *
     * @return 包含底层主体中所有 {@link UserClaim} 实例的列表。如果 subject 为 null 或没有声明，则返回空列表。
     */
    public List<UserClaim> getAllClaims() {
        if (subject == null) {
            return Collections.emptyList();
        }
        return subject.getPrincipals().stream()
                      .filter(principal -> principal instanceof UserClaim)
                      .map(principal -> (UserClaim) principal)
                      .collect(Collectors.toList());
    }

    // endregion

    // region 认证与角色检查

    /**
     * 检查用户是否已认证。
     *
     * @return 如果已认证则返回 true
     */
    public boolean isAuthenticated() {
        return subject != null && !subject.getPrincipals().isEmpty();
    }

    /**
     * 检查用户是否拥有指定角色。
     *
     * @param role 角色名称
     * @return 如果用户已认证且拥有该角色则返回 true
     */
    public boolean isInRole(String role) {
        return isAuthenticated() && subject.getPrincipals().stream()
                                           .anyMatch(principal -> principal != null &&
                                               principal.getName().equals(role));
    }

    /**
     * 检查用户是否拥有任意一个指定角色。
     *
     * @param roles 角色名称数组
     * @return 如果用户已认证且至少拥有一个指定角色则返回 true。
     *         如果 roles 为 null 或空，则返回 false。
     */
    public boolean isInRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        return isAuthenticated() && Arrays.stream(roles).anyMatch(this::isInRole);
    }

    /**
     * 检查用户是否拥有由分隔字符串指定的任一角色。
     *
     * @param roles     包含角色名称的分隔字符串（例如："Admin,User"）
     * @param separator 用于分隔角色的字符
     * @return 如果用户已认证且至少拥有一个解析出的角色则返回 true
     */
    public boolean isInRoles(String roles, char separator) {
        Objects.requireNonNull(roles, "roles must not be null");
        String[] parts = roles.split(java.util.regex.Pattern.quote(String.valueOf(separator)));
        return isInRoles(parts);
    }

    /**
     * 检查用户是否拥有指定角色。
     * <p>
     * 此方法委托给 {@link #isInRole(String)}，保留以提供向后兼容性。
     * </p>
     *
     * @param role 角色名称
     * @return 如果用户拥有该角色则返回 true
     * @see #isInRole(String)
     * @deprecated 使用 {@link #isInRole(String)} 以保持与 .NET 版本的命名一致。
     */
    @Deprecated
    public boolean hasRole(String role) {
        return isInRole(role);
    }

    // endregion

    // region 断言方法

    /**
     * 确保用户已认证，否则抛出 {@link AuthenticationException}。
     */
    public void ensureAuthenticated() {
        if (!isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }
    }

    /**
     * 确保用户拥有指定角色，否则抛出 {@link UnauthorizedAccessException}。
     *
     * @param role 角色名称
     */
    public void ensureHasRole(String role) {
        ensureAuthenticated();

        if (!isInRole(role)) {
            throw new UnauthorizedAccessException("User does not have required role: " + role);
        }
    }

    /**
     * 确保用户至少拥有其中一个指定角色，否则抛出 {@link UnauthorizedAccessException}。
     *
     * @param roles 角色名称数组
     */
    public void ensureInRoles(String... roles) {
        ensureAuthenticated();
        if (!isInRoles(roles)) {
            throw new UnauthorizedAccessException(
                "User does not have any of the required roles: " + String.join(", ", roles));
        }
    }

    /**
     * 确保用户拥有指定的声明（Claim），否则抛出 {@link UnauthorizedAccessException}。
     *
     * @param claimType 声明类型，参考 {@link UserClaimTypes} 中的常量
     */
    public void ensureHasClaim(String claimType) {
        if (findClaim(claimType) == null) {
            throw new UnauthorizedAccessException("User does not have required claim: " + claimType);
        }
    }

    // endregion
}
