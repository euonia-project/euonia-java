package com.euonia.bus;

import com.euonia.bus.recipient.Consumer;

/**
 * 内存消息总线的单播（点对点）消息接收者。
 * <p>
 * 继承自 {@link InMemoryRecipient}，实现 {@link Consumer} 接口，
 * 用于处理单播类型的消息。通过 {@link HandlerContext} 将消息分发给实际的消息处理器，并在处理完成后将任何异常记录到日志中（不会中断消息处理）。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class InMemoryUnicastRecipient extends InMemoryRecipient implements Consumer {

    /**
     * 创建单播接收者实例。
     *
     * @param handler 处理器上下文
     */
    public InMemoryUnicastRecipient(HandlerContext handler) {
        super(handler);
    }

    /**
     * 获取接收者名称。
     *
     * @return 接收者的简单类名
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
