package com.euonia.bus.message;

public record MessageHandlerContext(Class<?> handlerType, MessageHandlerFactory factory) {

}
