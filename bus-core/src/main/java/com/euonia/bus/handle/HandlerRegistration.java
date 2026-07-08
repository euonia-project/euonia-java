package com.euonia.bus.handle;

/**
 * 处理器注册信息，包含处理器类型和对应的工厂实例。
 *
 * @param handlerType 处理器类型
 * @param factory     用于创建处理器委托的工厂
 * @author damon(zhaorong@outlook.com)
 */
public record HandlerRegistration(Class<?> handlerType, HandlerFactory factory) {

}
