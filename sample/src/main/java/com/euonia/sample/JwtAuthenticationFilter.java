package com.euonia.sample;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * JWT 认证过滤器。
 * <p>
 * 该过滤器从 HTTP 请求的 Authorization 头中提取 JWT，并验证其签名和有效性。
 * 如果验证成功，会将用户信息存入 Spring Security 的上下文中。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${security.jwt.secret}")
    private String signingKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authorization.substring(7);
            try {
                // Validate signing key is configured
                if (signingKey == null || signingKey.isBlank()) {
                    LOGGER.warn("JWT signing key is not properly configured. Using default or empty key.");
                }

                var payload = Jwts.parser()
                                 .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8)))
                                 .build()
                                 .parseSignedClaims(token)
                                 .getPayload();

                String userId = payload.getSubject();
                if (userId != null && !userId.isBlank()) {
                    // 将 JWT 中的所有声明提取到 Map 中，以便后续 HttpContextFilter 构建 UserPrincipal
                    Map<String, Object> claims = new LinkedHashMap<>();
                    payload.forEach(claims::put);

                    var authentication = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authentication.setDetails(claims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    LOGGER.debug("JWT authentication successful for user: {}", userId);
                } else {
                    LOGGER.debug("JWT token has no subject (userId)");
                }
            } catch (SignatureException e) {
                // Don't throw an exception when token parsing fails, let the subsequent authentication process handle it and return 401.
                LOGGER.debug("JWT signature validation failed: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                LOGGER.debug("JWT token has expired: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                LOGGER.debug("JWT token is malformed: {}", e.getMessage());
            } catch (JwtException | IllegalArgumentException e) {
                LOGGER.debug("JWT parse failed: {}", e.getMessage());
            }
        } else if (authorization == null) {
            LOGGER.trace("No Authorization header found in request");
        } else if (!authorization.startsWith("Bearer ")) {
            LOGGER.debug("Authorization header does not start with 'Bearer '");
        }

        filterChain.doFilter(request, response);
    }
}
