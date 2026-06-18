package com.euonia.bus.messenger;

/**
 * 抽象分发器，在目标接收者上使用给定的消息调用 {@link MessageHandler} 回调。
 * <p>
 * 使用抽象类分发（虚方法）而非接口分发以获得更好的性能。
 *
 * <p>
 * 包含两个子类：
 * <ul>
 * <li>{@code For} — 包装类型化的 {@link MessageHandler}</li>
 * <li>{@code NullDispatcher} — 标记接收者实现了 {@link Recipient} 接口</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class MessageHandlerDispatcher {

    /**
     * 在指定的接收者上使用给定的消息调用处理器。
     *
     * @param recipient 接收者
     * @param message   消息
     */
    public abstract void invoke(Object recipient, Object message);

    /**
     * 单例标记分发器，表示接收者实现了 {@link Recipient} 接口，
     * 应直接通过 {@code Recipient.receive()} 进行调用。
     */
    static final MessageHandlerDispatcher NULL = new MessageHandlerDispatcher() {
        @Override
        public void invoke(Object recipient, Object message) {
            throw new UnsupportedOperationException("NULL dispatcher should never be invoked directly");
        }

        @Override
        public String toString() {
            return "NULL_DISPATCHER";
        }
    };

    /**
     * {@link MessageHandler} 的类型化包装器。
     *
     * @param <TRecipient> 接收者类型
     * @param <TMessage>   消息类型
     */
    public static final class For<TRecipient, TMessage> extends MessageHandlerDispatcher {

        /** 类型化的消息处理器。 */
        private final MessageHandler<TRecipient, TMessage> handler;

        /**
         * 使用指定的处理器构造新实例。
         *
         * @param handler 消息处理器
         */
        public For(MessageHandler<TRecipient, TMessage> handler) {
            this.handler = handler;
        }

        /**
         * 在指定的接收者上使用给定的消息调用处理器。
         *
         * @param recipient 接收者
         * @param message   消息
         */
        @SuppressWarnings("unchecked")
        @Override
        public void invoke(Object recipient, Object message) {
            handler.handle((TRecipient) recipient, (TMessage) message);
        }

        /**
         * 获取底层消息处理器。
         *
         * @return 消息处理器
         */
        public MessageHandler<TRecipient, TMessage> getHandler() {
            return handler;
        }
    }
}
