package com.euonia.osba;

/**
 * 表示一个保存事件的参数类，包含保存操作的结果信息，如新对象、错误信息和用户状态。
 *
 * @author damon(zhaorong@outlook)
 */
public class SavedEventArgs {
    private final Object newObject;
    private Object userState;
    private Throwable error;

    /**
     * 创建一个新的 SavedEventArgs 实例，包含保存操作的结果信息。
     *
     * @param newObject 保存操作生成的新对象
     */
    public SavedEventArgs(Object newObject) {
        this.newObject = newObject;
    }

    /**
     * 创建一个新的 SavedEventArgs 实例，包含保存操作的结果信息和用户状态。
     *
     * @param newObject 保存操作生成的新对象
     * @param error     保存操作过程中发生的错误
     * @param userState 用户状态信息
     */
    public SavedEventArgs(Object newObject, Throwable error, Object userState) {
        this.newObject = newObject;
        this.error = error;
        this.userState = userState;
    }

    /**
     * 获取保存操作生成的新对象。
     *
     * @return 保存操作生成的新对象
     */
    public Object getNewObject() {
        return newObject;
    }

    /**
     * 获取保存操作过程中发生的错误信息，如果没有错误则返回 null。
     *
     * @return 保存操作过程中发生的错误信息，如果没有错误则返回 null
     */
    public Throwable getError() {
        return error;
    }

    /**
     * 获取与保存操作相关的用户状态信息，可以用于在保存完成后执行特定的用户界面更新或其他操作。
     *
     * @return 与保存操作相关的用户状态信息
     */
    public Object getUserState() {
        return userState;
    }
}
