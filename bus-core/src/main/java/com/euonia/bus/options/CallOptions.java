package com.euonia.bus.options;

/**
 * RPC 调用选项，继承自 {@link ExtendableOptions}，额外包含关联 ID。
 * <p>
 * 用于请求-响应（call）模式的调用配置。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class CallOptions extends ExtendableOptions {
    /** 关联标识符，用于将请求与响应匹配 */
    private String correlationId;

    /**
     * 获取关联 ID。
     *
     * @return 关联 ID
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * 设置关联 ID。
     *
     * @param correlationId 关联 ID
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
