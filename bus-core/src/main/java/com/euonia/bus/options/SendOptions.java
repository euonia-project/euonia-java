package com.euonia.bus.options;

/**
 * 发送选项，继承自 {@link ExtendableOptions}，额外包含关联 ID。
 * <p>
 * 用于单播（send）模式的发送配置。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class SendOptions extends ExtendableOptions {
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
