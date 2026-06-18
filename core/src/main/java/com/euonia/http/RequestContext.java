package com.euonia.http;

import java.util.Map;

import com.euonia.security.UserPrincipal;

/**
 * 包含当前请求的相关信息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RequestContext {

    private String connectionId;
    private String requestUri;
    private String requestMethod;
    private String remoteIpAddress;
    private int remotePort;
    private boolean webSocketRequest;
    private UserPrincipal user;
    private Map<String, String> requestHeaders;
    private String traceIdentifier;

    /**
     * 获取表示连接的唯一标识符。
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * 设置表示连接的唯一标识符。
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * 获取请求的 URI。
     */
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * 设置请求的 URI。
     */
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    /**
     * 获取请求的方法（如 GET、POST 等）。
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * 设置请求的方法（如 GET、POST 等）。
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * 获取或设置远程目标的 IP 地址。可以为 null。
     */
    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    /**
     * 设置远程目标的 IP 地址。可以为 null。
     */
    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    /**
     * 获取或设置远程目标的端口。
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * 设置远程目标的端口。
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * 获取一个值，指示该请求是否为 WebSocket 建立请求。
     */
    public boolean isWebSocketRequest() {
        return webSocketRequest;
    }

    /**
     * 设置一个值，指示该请求是否为 WebSocket 建立请求。
     */
    public void setWebSocketRequest(boolean webSocketRequest) {
        this.webSocketRequest = webSocketRequest;
    }

    /**
     * 获取或设置此请求的用户。
     */
    public UserPrincipal getUser() {
        return user;
    }

    /**
     * 设置此请求的用户。
     */
    public void setUser(UserPrincipal user) {
        this.user = user;
    }

    /**
     * 获取请求头。
     */
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * 获取 Authorization HTTP 头。
     */
    public String getAuthorization() {
        return requestHeaders != null ? requestHeaders.get("Authorization") : null;
    }

    /**
     * 获取 Request-Id HTTP 头。
     */
    public String getRequestId() {
        return requestHeaders != null ? requestHeaders.get("Request-Id") : null;
    }

    /**
     * 获取或设置一个唯一标识符，用于在跟踪日志中表示此请求。
     */
    public String getTraceIdentifier() {
        return traceIdentifier;
    }

    /**
     * 设置一个唯一标识符，用于在跟踪日志中表示此请求。
     */
    public void setTraceIdentifier(String traceIdentifier) {
        this.traceIdentifier = traceIdentifier;
    }
}
