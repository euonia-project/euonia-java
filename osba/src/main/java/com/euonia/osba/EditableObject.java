package com.euonia.osba;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.euonia.osba.abstracts.Savable;
import com.euonia.osba.rules.BrokenRule;
import com.euonia.osba.rules.RuleCheckException;

/**
 * 表示可编辑的业务对象，可以保存到数据库或其他持久化存储中。
 * 此类继承 ObservableObject 并实现 Savable 接口，为 save 方法提供包含验证和事件处理的默认实现。
 *
 * @param <T> 可编辑对象的类型
 * @author damon(zhaorong@outlook)
 */
public abstract class EditableObject<T extends EditableObject<T>> extends ObservableObject<T> implements Savable<T> {

    private final SubmissionPublisher<SavedEventArgs> savedEventPublisher = new SubmissionPublisher<>();

    /**
     * 订阅 onSaved 事件的监听器，该事件在对象保存时触发。
     * 监听器将接收一个 SavedEventArgs 对象，其中包含已保存对象的信息、保存过程中发生的任何错误以及传递给 save 方法的任何用户状态。
     *
     * @param listener 对象保存时要通知的监听器
     */
    public final void onSaved(Consumer<SavedEventArgs> listener) {
        assert listener != null : "Listener cannot be null.";
        savedEventPublisher.consume(listener);
    }

    /**
     * 触发 onSaved 事件，通知所有订阅的监听器对象已保存。
     *
     * @param newObject 已保存的对象
     * @param error     保存过程中发生的任何错误
     * @param userState 传递给 onSaved 事件的附加信息
     */
    protected void onSaved(T newObject, Throwable error, Object userState) {
        savedEventPublisher.submit(new SavedEventArgs(newObject, error, userState));
    }

    /**
     * 将当前对象保存到数据库或其他持久化存储中，并在保存完成后触发 onSaved 事件。
     *
     * @param newObject 已保存的对象
     */
    @Override
    public void saveComplete(T newObject) {
        onSaved(newObject, null, null);
    }

    /**
     * 将当前对象保存到数据库或其他持久化存储中。
     *
     * @param forceUpdate 是否强制更新，即使对象未被标记为已更改
     * @return 已保存的对象
     */
    @Override
    public T save(boolean forceUpdate) {
        return save(forceUpdate, null);
    }

    /**
     * 将当前对象保存到数据库或其他持久化存储中。
     * 如果 forceUpdate 为 true，即使对象未被标记为已更改，也会保存该对象。
     * userState 参数可用于向 onSaved 事件传递附加信息。
     *
     * @param forceUpdate 是否强制更新，即使对象未被标记为已更改
     * @param userState   传递给 onSaved 事件的附加信息
     * @return 已保存的对象
     */
    @SuppressWarnings({"SameParameterValue", "unchecked"})
    protected T save(boolean forceUpdate, Object userState) {
        if (getState() == ObjectEditState.NONE) {
            if (forceUpdate) {
                markAsChanged();
            } else {
                return (T) this;
            }
        }

        CompletableFuture<List<String>> validations;

        if (!isDeleted() || isCheckObjectRulesOnDelete()) {
            validations = getRules().checkObjectRulesAsync();
        } else {
            validations = CompletableFuture.completedFuture(List.of());
        }

        return validations.thenCompose(ignored -> {
            if (!isValid() && (!isDeleted() || isCheckObjectRulesOnDelete())) {
                var errors = getRules().getBrokenRules()
                                       .stream()
                                       .collect(Collectors.groupingBy(BrokenRule::getProperty, Collectors.mapping(BrokenRule::getDescription, Collectors.toList())));
                return CompletableFuture.failedFuture(new RuleCheckException(errors));
            }

            return CompletableFuture.supplyAsync(() -> {
                try {
                    markAsBusy();
                    T result = getBusinessContext().getObjectFactory().save((Class<T>) getClass(), (T) this);
                    onSaved(result, null, userState);
                    return result;
                } catch (Throwable error) {
                    onSaved(null, error, userState);
                    throw new RuntimeException(error);
                } finally {
                    markAsIdle();
                }
            });
        }).join();
    }

    /**
     * 预定义的create方法，子类可以覆盖此方法以实现对象的创建逻辑，例如设置初始属性值或触发创建事件。
     */
    protected void create() {
    }

    /**
     * 预定义的insert方法，子类可以覆盖此方法以实现对象插入数据库或其他持久化存储的逻辑，例如执行数据库操作或触发插入事件。
     */
    protected void insert() {
    }

    /**
     * 预定义的update方法，子类可以覆盖此方法以实现对象更新数据库或其他持久化存储的逻辑，例如执行数据库操作或触发更新事件。
     */
    protected void update() {
    }

    /**
     * 预定义的delete方法，子类可以覆盖此方法以实现对象删除数据库或其他持久化存储的逻辑，例如执行数据库操作或触发删除事件。
     */
    protected void delete() {
    }

    @Override
    public void close() {
        super.close();
        savedEventPublisher.close();
    }
}
