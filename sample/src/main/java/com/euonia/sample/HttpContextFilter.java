package com.euonia.sample;

import com.euonia.core.ObjectId;
import com.euonia.core.PriorityValueFinder;
import com.euonia.http.RequestContext;
import com.euonia.http.RequestContextAccessor;
import com.euonia.security.UserClaim;
import com.euonia.security.UserClaimTypes;
import com.euonia.security.UserPrincipal;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;

import org.apache.catalina.connector.RequestFacade;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * HTTP 上下文过滤器，从请求中提取信息并构建 {@link RequestContext}。
 * <p>
 * 按优先级从多个来源获取用户身份信息，包括 Spring Security、
 * Tomcat 原生认证以及 JWT 回退解析，将声明统一封装为 {@link UserPrincipal}。
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
@Component
@Order(0)
public class HttpContextFilter implements Filter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            var facade = getRequestFacade(request);
            var context = getRequestContext(facade);
            RequestContextAccessor.set(context);
            chain.doFilter(request, response);
        } finally {
            RequestContextAccessor.remove();
        }
    }

    /**
     * 递归获取 RequestFacade 对象，支持 ServletRequestWrapper 包装的请求。
     *
     * @param request Servlet 请求对象
     * @return RequestFacade 对象
     */
    private RequestFacade getRequestFacade(ServletRequest request) {
        switch (request) {
            case RequestFacade facade -> {
                return facade;
            }
            case ServletRequestWrapper wrapper -> {
                return getRequestFacade(wrapper.getRequest());
            }
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getClass().getName());
        }
    }

    // region RequestContext 构建

    private RequestContext getRequestContext(RequestFacade facade) {
        var requestContext = new RequestContext();
        requestContext.setRequestUri(facade.getRequestURI());
        requestContext.setRemotePort(facade.getRemotePort());
        requestContext.setRequestMethod(facade.getMethod());
        requestContext.setRemoteIpAddress(facade.getRemoteAddr());
        requestContext.setTraceIdentifier(ObjectId.newGuid().toString());

        Map<String, String> headers = Collections.list(facade.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        headerName -> String.join(",", Collections.list(facade.getHeaders(headerName)))));

        requestContext.setRequestHeaders(headers);

        // 从请求中读取用户主体信息并生成 UserPrincipal
        var principal = getPrincipal(facade);
        if (principal != null) {
            var subject = buildSubject(principal);
            requestContext.setUser(new UserPrincipal(subject));
        }

        return requestContext;
    }

    // endregion

    // region Subject 构建 —— 从 Principal 中提取声明并生成 UserClaim

    /**
     * 从 {@link Principal} 中提取所有可用声明，构建包含 {@link UserClaim} 的
     * {@link javax.security.auth.Subject}。
     * <p>
     * 支持两种来源：
     * <ul>
     * <li>Spring Security {@link Authentication} —— 提取用户 ID、角色及 details 中的 JWT
     * claims</li>
     * <li>普通 {@link Principal} —— 至少将 name 映射为 SUBJECT</li>
     * </ul>
     * </p>
     *
     * @param principal 用户主体信息
     * @return 包含所有提取声明的 Subject
     */
    private Subject buildSubject(Principal principal) {
        Set<Principal> principals = new HashSet<>();

        // 保留原始主体，确保 getName() 等基础方法正常工作
        principals.add(principal);

        switch (principal) {
            case Authentication auth -> {
                // 用户 ID（sub）
                String userId = auth.getName();
                if (userId != null && !userId.isBlank()) {
                    principals.add(new UserClaim(UserClaimTypes.SUBJECT, userId));
                }

                // 从 JWT claims（存储在 details 中的 Map）提取所有标准声明
                if (auth.getDetails() instanceof Map<?, ?> claims) {
                    extractStandardClaims(principals, claims);
                }

                // 从 GrantedAuthority 中提取角色
                extractAuthoritiesAsRoles(principals, auth);
            }
            case null -> {
                // 无主体信息，返回空 Subject
            }
            default -> {
                // 普通 Principal：将 name 作为 SUBJECT
                String name = principal.getName();
                if (name != null && !name.isBlank()) {
                    principals.add(new UserClaim(UserClaimTypes.SUBJECT, name));
                }
            }
        }

        return new javax.security.auth.Subject(
                true,
                principals,
                Collections.emptySet(),
                Collections.emptySet());
    }

    /**
     * 从 JWT claims Map 中提取标准 OIDC 声明。
     */
    private void extractStandardClaims(Set<Principal> principals, Map<?, ?> claims) {
        // 单值字符串声明
        extractClaim(principals, claims, UserClaimTypes.SUBJECT);
        extractClaim(principals, claims, UserClaimTypes.NAME);
        extractClaim(principals, claims, UserClaimTypes.PREFERRED_USER_NAME);
        extractClaim(principals, claims, UserClaimTypes.GIVEN_NAME);
        extractClaim(principals, claims, UserClaimTypes.FAMILY_NAME);
        extractClaim(principals, claims, UserClaimTypes.NICKNAME);
        extractClaim(principals, claims, UserClaimTypes.EMAIL);
        extractClaim(principals, claims, UserClaimTypes.TENANT);
        extractClaim(principals, claims, UserClaimTypes.CODE);
        extractClaim(principals, claims, UserClaimTypes.CLIENT_ID);
        extractClaim(principals, claims, UserClaimTypes.IDENTITY_PROVIDER);
        extractClaim(principals, claims, UserClaimTypes.SCHEME);
        extractClaim(principals, claims, UserClaimTypes.LOCALE);
        extractClaim(principals, claims, UserClaimTypes.ZONE_INFO);
        extractClaim(principals, claims, UserClaimTypes.PROFILE);
        extractClaim(principals, claims, UserClaimTypes.WEBSITE);
        extractClaim(principals, claims, UserClaimTypes.PICTURE);
        extractClaim(principals, claims, UserClaimTypes.GENDER);
        extractClaim(principals, claims, UserClaimTypes.BIRTHDATE);
        extractClaim(principals, claims, UserClaimTypes.PHONE_NUMBER);
        extractClaim(principals, claims, UserClaimTypes.SESSION_ID);
        extractClaim(principals, claims, UserClaimTypes.SCOPE);

        // 角色声明 —— 可能是字符串、列表或 JSON 数组
        extractRoleClaims(principals, claims, UserClaimTypes.ROLE);

        // scope 声明可能包含空格分隔的多个 scope
        extractScopeClaims(principals, claims);

        // Keycloak 风格的 realm_access.roles
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            extractRoleClaims(principals, realmAccessMap, "roles");
        }
    }

    /**
     * 提取单值字符串声明。
     */
    private void extractClaim(Set<java.security.Principal> principals, Map<?, ?> claims, String claimType) {
        Object value = claims.get(claimType);
        if (value instanceof String str && !str.isBlank()) {
            principals.add(new UserClaim(claimType, str));
        }
    }

    /**
     * 提取角色声明，支持以下格式：
     * <ul>
     * <li>字符串：{@code "role": "admin"}</li>
     * <li>JSON 数组字符串：{@code "role": "[\"admin\",\"user\"]"}</li>
     * <li>列表：{@code "role": ["admin", "user"]}</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private void extractRoleClaims(Set<java.security.Principal> principals, Map<?, ?> claims, String claimKey) {
        Object value = claims.get(claimKey);
        switch (value) {
            case String str -> {
                if (str.isBlank()) {
                    return;
                }
                // 尝试解析为 JSON 数组，如 ["admin","user"]
                if (str.startsWith("[") && str.endsWith("]")) {
                    try {
                        List<String> roles = OBJECT_MAPPER.readValue(str, List.class);
                        for (String role : roles) {
                            if (role != null && !role.isBlank()) {
                                principals.add(new UserClaim(UserClaimTypes.ROLE, role));
                            }
                        }
                    } catch (JsonProcessingException e) {
                        // 解析失败则作为单个角色处理
                        principals.add(new UserClaim(UserClaimTypes.ROLE, str));
                    }
                } else {
                    principals.add(new UserClaim(UserClaimTypes.ROLE, str));
                }
            }
            case List<?> list -> {
                for (Object item : list) {
                    if (item instanceof String role && !role.isBlank()) {
                        principals.add(new UserClaim(UserClaimTypes.ROLE, role));
                    }
                }
            }
            default -> {
                // 忽略无法识别的类型
            }
        }
    }

    /**
     * 提取 scope 声明。scope 可能是空格分隔的字符串（如 "openid profile email"）。
     */
    private void extractScopeClaims(Set<java.security.Principal> principals, Map<?, ?> claims) {
        Object scopeValue = claims.get(UserClaimTypes.SCOPE);
        if (scopeValue instanceof String scope && !scope.isBlank()) {
            // 按空格分隔 scope
            String[] scopes = scope.split("\\s+");
            for (String s : scopes) {
                if (!s.isBlank()) {
                    principals.add(new UserClaim(UserClaimTypes.SCOPE, s));
                }
            }
        }
    }

    /**
     * 从 Spring Security 的 GrantedAuthority 中提取角色。
     * 自动去除 {@code ROLE_} 前缀。
     */
    private void extractAuthoritiesAsRoles(Set<java.security.Principal> principals, Authentication auth) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();
            if (role != null && !role.isBlank()) {
                // Spring Security 角色通常带有 "ROLE_" 前缀，去掉前缀存储
                String roleName = role.startsWith("ROLE_") ? role.substring(5) : role;
                principals.add(new UserClaim(UserClaimTypes.ROLE, roleName));
            }
        }
    }

    // endregion

    // region Principal 获取 —— 多来源优先级回退

    /**
     * 按优先级从多个来源获取用户主体信息。
     * <ol>
     * <li>Spring Security SecurityContext 中的 Authentication</li>
     * <li>Tomcat 原生 RequestFacade 中的 UserPrincipal</li>
     * <li>HttpSession 中的 Spring Security 认证信息</li>
     * <li>从 Authorization 头直接解析 JWT</li>
     * <li>从 Cookie 中解析 JWT</li>
     * </ol>
     */
    private Principal getPrincipal(RequestFacade facade) {
        return PriorityValueFinder.find(queue -> {
            // 优先级 0：从 Spring Security 的 SecurityContext 获取当前认证信息
            // JwtAuthenticationFilter 已将 JWT 解析结果设置到 SecurityContextHolder 中
            queue.add(() -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                    return auth;
                }
                return null;
            }, 0);

            // 优先级 1：尝试从原生 RequestFacade 获取（兼容 Tomcat 原生认证场景）
            queue.add(facade::getUserPrincipal, 1);

            // 优先级 2：尝试从 HttpSession 中获取 Spring Security 的认证信息
            queue.add(() -> {
                var session = facade.getSession(false);
                if (session == null) {
                    return null;
                }
                var attribute = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (!(attribute instanceof SecurityContext securityContext)) {
                    return null;
                }
                return securityContext.getAuthentication();
            }, 2);

            // 优先级 3：回退 —— 从 Authorization 头直接解析 JWT
            queue.add(() -> {
                var authorization = facade.getHeader("Authorization");
                if (authorization == null || !authorization.startsWith("Bearer ")) {
                    return null;
                }
                String token = authorization.substring(7);
                return parseJwtToPrincipal(token);
            }, 3);

            // 优先级 4：回退 —— 从 Cookie 中解析 JWT
            queue.add(() -> {
                Cookie[] cookies = facade.getCookies();
                if (cookies == null) {
                    return null;
                }
                for (Cookie cookie : cookies) {
                    if (Objects.equals(cookie.getName(), "AUTH_TOKEN")) {
                        return parseJwtToPrincipal(cookie.getValue());
                    }
                }
                return null;
            }, 4);
        }, Objects::nonNull, null);
    }

    // endregion

    // region JWT 回退解析

    /**
     * 直接解析 JWT（不验证签名），将其中包含的声明提取为 {@link Principal}。
     * <p>
     * 此方法作为回退路径，仅在 Spring Security 未处理令牌时调用。
     * 通过 Base64 解码 JWT 载荷来提取声明，不进行签名验证。
     * </p>
     *
     * @param token JWT 令牌字符串
     * @return 包含声明信息的 Authentication，解析失败则返回 null
     */
    private Principal parseJwtToPrincipal(String token) {
        try {
            Map<String, Object> claims = decodeJwtPayload(token);
            if (claims == null || claims.isEmpty()) {
                return null;
            }

            // 使用 sub 作为用户名，若不存在则尝试其他常见标识
            String username = (String) claims.getOrDefault(UserClaimTypes.SUBJECT,
                    claims.getOrDefault(UserClaimTypes.PREFERRED_USER_NAME,
                            claims.getOrDefault(UserClaimTypes.EMAIL, "unknown")));

            var authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            authentication.setDetails(claims);
            return authentication;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解码 JWT 载荷（Payload）为声明 Map。
     * <p>
     * JWT 格式为 {@code header.payload.signature}，各部分使用 Base64Url 编码。
     * 此方法仅解码载荷部分，不验证签名。
     * </p>
     *
     * @param token JWT 令牌字符串
     * @return 声明 Map，解析失败则返回 null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            // JWT payload 使用 Base64Url 编码（无填充），需要处理
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    // endregion
}
