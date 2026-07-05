package com.euonia.bus.handle;

public record HandlerRegistration(Class<?> handlerType, HandlerFactory factory) {

}
