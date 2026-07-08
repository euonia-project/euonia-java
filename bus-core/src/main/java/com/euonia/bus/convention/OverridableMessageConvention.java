package com.euonia.bus.convention;

import java.util.function.Predicate;

/**
 * 可覆盖的消息约定实现，包装一个内部 {@link MessageConvention}，允许通过断言覆盖其判断逻辑。
 * <p>
 * 如果为某种消息类型（单播/多播/请求）设置了覆盖断言，则优先使用断言；否则回退到内部约定的判断。
 *
 * @author damon(zhaorong@outlook.com)
 */
class OverridableMessageConvention implements MessageConvention {
    /**
     * 内部委托的约定
     */
    private final MessageConvention innerConvention;
    /**
     * 单播覆盖断言
     */
    private Predicate<String> unicastPredicate;
    /**
     * 多播覆盖断言
     */
    private Predicate<String> multicastPredicate;
    /**
     * 请求覆盖断言
     */
    private Predicate<String> requestPredicate;

    /**
     * 使用内部约定构造可覆盖约定。
     *
     * @param innerConvention 内部委托的约定
     */
    OverridableMessageConvention(MessageConvention innerConvention) {
        this.innerConvention = innerConvention;
    }

    @Override
    public String getName() {
        return String.format("Override with %s", innerConvention.getName());
    }

    @Override
    public boolean isUnicast(String channel) {
        if (unicastPredicate == null) {
            return innerConvention.isUnicast(channel);
        } else {
            return unicastPredicate.test(channel);
        }
    }

    @Override
    public boolean isMulticast(String channel) {
        if (multicastPredicate == null) {
            return innerConvention.isMulticast(channel);
        } else {
            return multicastPredicate.test(channel);
        }
    }

    @Override
    public boolean isRequest(String channel) {
        if (requestPredicate == null) {
            return innerConvention.isRequest(channel);
        } else {
            return requestPredicate.test(channel);
        }
    }

    /**
     * 设置单播断言覆盖。
     *
     * @param unicastPredicate 单播断言
     */
    public void setUnicastPredicate(Predicate<String> unicastPredicate) {
        this.unicastPredicate = unicastPredicate;
    }

    /**
     * 设置多播断言覆盖。
     *
     * @param multicastPredicate 多播断言
     */
    public void setMulticastPredicate(Predicate<String> multicastPredicate) {
        this.multicastPredicate = multicastPredicate;
    }

    /**
     * 设置请求断言覆盖。
     *
     * @param requestPredicate 请求断言
     */
    public void setRequestPredicate(Predicate<String> requestPredicate) {
        this.requestPredicate = requestPredicate;
    }
}
