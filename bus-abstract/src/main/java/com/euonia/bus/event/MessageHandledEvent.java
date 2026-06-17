package com.euonia.bus.event;

public class MessageHandledEvent extends MessageProcessedEvent {
    private final Object message;
    private Class<?> handlerType;

    public MessageHandledEvent(Object message) {
        super(message, null, MessageProcessType.HANDLED);
        this.message = message;
    }

    public Class<?> getHandlerType() {
        return handlerType;
    }

    public void setHandlerType(Class<?> handlerType) {
        this.handlerType = handlerType;
    }

    @Override
    public Object getMessage() {
        return message;
    }
}
