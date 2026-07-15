package com.euonia.domain.event;

/**
 * {@link ApplicationEventBase} 是应用事件的抽象基类实现。它继承自 {@link EventBase} 并实现 {@link ApplicationEvent} 接口，为领域模型中所有应用特定的事件提供公共基础。
 * <p>
 * 具体的应用事件类可以继承此类，从而实现一致的应用程序事件处理和加工，同时保留根据需要定义事件特定属性和行为的灵活性。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class ApplicationEventBase extends EventBase implements ApplicationEvent {
}
