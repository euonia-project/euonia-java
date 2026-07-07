package com.euonia.domain.event;

import com.euonia.bus.message.Multicast;

/**
 * {@link Event} 接口表示领域模型中的通用事件。定义了所有事件应具备的基本属性和方法。
 * <p>
 * 事件用于捕获和表示系统中的重要发生事件或变更，允许不同组件之间的通信和协调。
 * 继承自 {@link Multicast}，表明事件以多播模式传播。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Event extends Multicast {
    /**
     * 获取事件的序号，可用于确定事件的顺序。
     *
     * @return 事件的序号
     */
    long getSequence();

    /**
     * 设置事件的序号，可用于确定事件的顺序。
     *
     * @param sequence 事件的序号
     */
    void setSequence(long sequence);

    /**
     * 获取事件意图，表示事件的目的或含义，可用于分类或识别正在处理的事件类型。
     *
     * @return 事件的意图
     */
    String getEventIntent();

    /**
     * 设置事件意图，表示事件的目的或含义，可用于分类或识别正在处理的事件类型。
     *
     * @param eventIntent 事件的意图
     */
    void setEventIntent(String eventIntent);

    /**
     * 获取事件的发起者类型，表示事件的来源或起源。
     *
     * @return 事件的发起者类型
     */
    String getOriginatorType();

    /**
     * 设置事件的发起者类型，表示事件的来源或起源。
     *
     * @param originatorType 事件的发起者类型
     */
    void setOriginatorType(String originatorType);

    /**
     * 获取事件的发起者 ID，唯一标识事件的来源或起源。
     *
     * @return 事件的发起者 ID
     */
    String getOriginatorId();

    /**
     * 设置事件的发起者 ID，唯一标识事件的来源或起源。
     *
     * @param originatorId 事件的发起者 ID
     */
    void setOriginatorId(String originatorId);
}
