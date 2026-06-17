package com.euonia.bus;

/**
 * 定义用于内存传输的消息包，将路由消息与其处理上下文封装在一起。
 *
 * @author damon(zhaorong@outlook)
 */
public class MessagePack {

    private final RoutedMessage<?> message;
    private final MessageContext context;
    private boolean aborted;

    /**
     * 使用指定的消息和上下文初始化新实例。
     *
     * @param message 路由消息信封
     * @param context 消息处理上下文
     */
    public MessagePack(RoutedMessage<?> message, MessageContext context) {
        this.message = message;
        this.context = context;
    }

    /**
     * 获取路由消息信封。
     *
     * @return 路由消息
     */
    public RoutedMessage<?> getMessage() {
        return message;
    }

    /**
     * 获取消息处理上下文。
     *
     * @return 消息上下文
     */
    public MessageContext getContext() {
        return context;
    }

    /**
     * 获取消息是否已被中止。
     *
     * @return 如果已中止则返回 {@code true}
     */
    public boolean isAborted() {
        return aborted;
    }

    /**
     * 设置此消息包的中止标志。
     *
     * @param aborted 中止状态
     */
    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }
}
