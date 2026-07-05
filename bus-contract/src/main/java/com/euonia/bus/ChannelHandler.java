package com.euonia.bus;

import java.lang.reflect.Method;

/**
 * Represents a message handler, encapsulating the handler type, method, and instance.
 *
 * @param handlerType the type of the handler
 * @param method      the method to be invoked
 * @param instance    the instance of the handler
 */
public record ChannelHandler(Class<?> handlerType, Method method, Object instance) {
}
