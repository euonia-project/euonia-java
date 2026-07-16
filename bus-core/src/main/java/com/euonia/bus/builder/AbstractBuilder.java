package com.euonia.bus.builder;

import com.euonia.bus.MessageEnvelope;
import com.euonia.bus.MessageMetadata;
import com.euonia.bus.options.ExtendableOptions;
import com.euonia.core.ArgumentNullException;
import com.euonia.core.ArgumentOutOfRangeException;
import com.euonia.pipeline.Pipeline;

import java.util.function.Consumer;

/**
 * 抽象的 Builder 类，提供通用的配置方法。
 * <p>
 * 该类使用泛型参数 B 和 O，分别表示具体的 Builder 类型和配置选项类型。子类应继承该类并指定具体的类型。
 *
 * @param <B> 具体的 Builder 类型
 * @param <O> 配置选项类型
 * @param <T> 消息负载类型
 * @param <R> 响应类型
 * @author damon(zhaorong@outlook.com)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBuilder<B extends AbstractBuilder<B, O, T, R>, O extends ExtendableOptions, T, R> {
    protected final O options;
    protected Consumer<Pipeline<MessageEnvelope<T>, R>> behavior;

    protected AbstractBuilder(O options) {
        this.options = options;
    }

    /**
     * 指定消息所属通道。
     *
     * @param channel 消息通道
     * @return 当前的 Builder 实例
     */
    public B withChannel(String channel) {
        ArgumentNullException.throwIfNullOrEmpty(channel, "channel");
        options.setChannel(channel);
        return (B) this;
    }

    /**
     * 指定请求的 messageId。
     *
     * @param messageId 请求的 messageId
     * @return 当前的 Builder 实例
     */
    public B withMessageId(String messageId) {
        ArgumentNullException.throwIfNullOrEmpty(messageId, "messageId");
        options.setMessageId(messageId);
        return (B) this;
    }

    /**
     * 指定消息优先级，数值越大优先级越高。未指定时，系统会使用默认的优先级。
     *
     * @param priority 请求的优先级
     * @return 当前的 Builder 实例
     */
    public B withPriority(int priority) {
        options.setPriority(priority);
        return (B) this;
    }

    /**
     * 指定消息的超时时间，单位为毫秒。未指定时，系统会使用默认的超时时间。
     *
     * @param timeoutMillis 超时时间，单位为毫秒
     * @return 当前的 Builder 实例
     */
    public B withTimeout(long timeoutMillis) {
        ArgumentOutOfRangeException.throwIfNegative(timeoutMillis, "timeoutMillis");
        options.setTimeout(timeoutMillis);
        return (B) this;
    }

    /**
     * 指定消息的延迟发送时间，单位为毫秒。未指定时，消息会立即发送。
     *
     * @param delayMillis 延迟发送时间，单位为毫秒
     * @return 当前的 Builder 实例
     */
    public B withDelay(long delayMillis) {
        ArgumentOutOfRangeException.throwIfNegative(delayMillis, "delayMillis");
        options.setDelay(delayMillis);
        return (B) this;
    }

    /**
     * 指定消息的元数据设置器。
     *
     * @param metadataSetter 元数据设置器
     * @return 当前的 Builder 实例
     */
    public B withMetadata(Consumer<MessageMetadata> metadataSetter) {
        ArgumentNullException.throwIfNull(metadataSetter, "metadataSetter");
        options.setMetadataSetter(metadataSetter);
        return (B) this;
    }

    /**
     * 配置管道行为回调。
     *
     * @param behavior 管道行为回调
     * @return 当前的 Builder 实例
     */
    public B withBehavior(Consumer<Pipeline<MessageEnvelope<T>, R>> behavior) {
        ArgumentNullException.throwIfNull(behavior, "behavior");
        this.behavior = behavior;
        return (B) this;
    }
}
