package com.euonia.security;

import javax.security.auth.Subject;

/**
 * 用户主体，封装 {@link Subject} 并提供了方便的用户信息访问方法。
 * <p>
 * 支持获取用户名、检查认证状态、角色验证以及权限断言。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class UserPrincipal {
    /** 底层安全主题 */
    private final Subject subject;

    /**
     * 使用指定的 {@link Subject} 构造用户主体。
     *
     * @param subject 安全主题
     */
    public UserPrincipal(Subject subject) {
        this.subject = subject;
    }

    /**
     * 获取底层的 {@link Subject}。
     *
     * @return 安全主题
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名，如果不存在则返回 null
     */
    public String getName() {
        return subject.getPrincipals().stream()
                .filter(principal -> principal instanceof java.security.Principal)
                .map(principal -> ((java.security.Principal) principal).getName())
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取指定类型的声明（Claim）值。
     *
     * @param claimType 声明类型
     * @return 声明值，当前为占位实现始终返回 null
     */
    public String getClaim(String claimType) {
        // return subject.getPrincipals().stream()
        //         .filter(principal -> principal instanceof UserClaim)
        //         .map(principal -> (UserClaim) principal)
        //         .filter(claim -> claim.getType().equals(claimType))
        //         .map(UserClaim::getValue)
        //         .findFirst()
        //         .orElse(null);

        return null; // 占位，等待 UserClaim 实现添加后启用
    }

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
     * @return 如果用户拥有该角色则返回 true
     */
    public boolean hasRole(String role) {
        return subject != null && subject.getPrincipals().stream()
                .anyMatch(principal -> principal instanceof java.security.Principal &&
                        ((java.security.Principal) principal).getName().equals(role));
    }

    /**
     * 检查用户是否拥有任意一个指定角色。
     *
     * @param roles 角色名称数组
     * @return 如果用户拥有任意一个角色则返回 true
     */
    public boolean isInRoles(String... roles) {
        return subject != null && subject.getPrincipals().stream()
                .anyMatch(principal -> principal instanceof java.security.Principal &&
                        java.util.Arrays.asList(roles).contains(((java.security.Principal) principal).getName()));
    }

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

        if (subject.getPrincipals().stream()
                .noneMatch(principal -> principal instanceof java.security.Principal &&
                        ((java.security.Principal) principal).getName().equals(role))) {
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
        if (subject.getPrincipals().stream()
                .noneMatch(principal -> principal instanceof java.security.Principal &&
                        java.util.Arrays.asList(roles).contains(((java.security.Principal) principal).getName()))) {
            throw new UnauthorizedAccessException("User does not have any of the required roles: " + String.join(", ", roles));
        }
    }

    // public void ensureHasClaim(String claimType) {
    //     if (subject == null || subject.getPrincipals().stream()
    //             .noneMatch(principal -> principal instanceof UserClaim &&
    //                     ((UserClaim) principal).getType().equals(claimType))) {
    //         throw new UnauthorizedAccessException("User does not have required claim: " + claimType);
    //     }
    // }
}
