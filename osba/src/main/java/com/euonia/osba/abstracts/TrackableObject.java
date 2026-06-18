package com.euonia.osba.abstracts;

/**
 * TrackableObject 是一个接口，定义了用于跟踪对象状态的方法，包括其有效性、变更、删除状态以及是否为新建对象或是否可保存。
 * 此接口通常用于业务对象中，以管理其生命周期，并确保在执行保存或删除等操作之前对象处于一致的状态。
 * <p>
 * 此接口中定义的方法包括：
 * <p>
 * - isValid()：指示对象是否处于有效状态。用于确定对象是否可以保存。
 * - isChanged()：指示对象自上次保存以来是否已被更改。
 * - isDeleted()：指示对象是否已被标记为删除。
 * - isNew()：指示对象是否为新建对象且尚未保存。
 * - isSavable()：指示对象是否可以保存。
 * - isBusy()：指示对象当前是否处于忙碌状态，即正在执行不应被中断的操作，如保存或删除。
 * <p>
 * 实现此接口允许业务对象有效管理其状态，并确保仅在对象处于适当状态时才执行操作。
 * 例如，无效的对象不应保存，已标记为删除的对象不应更新。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface TrackableObject {

    /**
     * 指示对象是否处于有效状态。用于确定对象是否可以保存。
     *
     * @return 如果对象有效则返回 true，否则返回 false
     */
    boolean isValid();

    /**
     * 指示对象自上次保存以来是否已被更改。
     *
     * @return 如果对象已被更改则返回 true，否则返回 false
     */
    boolean isChanged();

    /**
     * 指示对象是否已被标记为删除。
     *
     * @return 如果对象已被标记为删除则返回 true，否则返回 false
     */
    boolean isDeleted();

    /**
     * 指示对象是否为新建对象且尚未保存。
     *
     * @return 如果对象是新建对象则返回 true，否则返回 false
     */
    boolean isNew();

    /**
     * 指示对象是否可以保存。
     *
     * @return 如果对象可以保存则返回 true，否则返回 false
     */
    boolean isSavable();

    /**
     * 指示对象当前是否处于忙碌状态，即正在执行不应被中断的操作，如保存或删除。
     *
     * @return 如果对象处于忙碌状态则返回 true，否则返回 false
     */
    boolean isBusy();
}
