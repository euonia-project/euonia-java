package com.euonia.bus;

import java.lang.reflect.Method;

/**
 * 表示一个消息处理器，封装了处理器类型、方法和实例。
 *
 * @param handlerType 处理器的类型
 * @param method      要调用的方法
 * @param instance    处理器的实例
 * @author damon(zhaorong@outlook.com)
 */
public record ChannelHandler(Class<?> handlerType, Method method, Object instance) {
}
