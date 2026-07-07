package com.euonia.domain.command;

import com.euonia.bus.message.Unicast;

/**
 * 命令标记接口，继承自 {@link Unicast}，表示该消息为单播命令。
 * <p>
 * 所有命令类型应实现此接口，以确保消息总线按单播模式路由命令消息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Command extends Unicast {
}
