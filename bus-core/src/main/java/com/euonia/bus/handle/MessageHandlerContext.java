package com.euonia.bus.handle;

public record MessageHandlerContext(Class<?> handlerType, MessageHandlerFactory factory) {

}
