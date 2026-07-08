package com.euonia.bus.recipient;

/**
 * 消息接收者基接口，所有类型的消息接收者（消费者、订阅者、执行器）的父接口。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Recipient {

    /**
     * 获取此接收者的唯一名称。
     *
     * @return 此接收者的名称，不能为 {@code null}
     */
    String getName();

}
