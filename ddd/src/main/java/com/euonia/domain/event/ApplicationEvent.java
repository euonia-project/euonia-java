package com.euonia.domain.event;

/**
 * {@link ApplicationEvent} 是应用事件的标记接口，用于在应用上下文中发布的事件。它继承自基础的 {@link Event} 接口，可用于分类特定于应用领域逻辑的事件。
 * <p>
 * 通过实现此接口，事件可以被设计用于处理应用特定事件的事件监听器轻松识别和处理，从而在应用的事件驱动架构中实现更好的组织和关注点分离。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface ApplicationEvent extends Event {
}
