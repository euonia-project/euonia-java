package com.euonia.reflection;

import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import com.euonia.osba.abstracts.TrackableObject;

/**
 * 表示对象的一个字段，可以跟踪其变化。
 * 此类提供了获取和设置字段值的方法，以及将其标记为未更改或撤销更改的方法。
 *
 * @param <T> 字段值的类型。
 * @author damon(zhaorong@outlook)
 */
public class FieldData<T> implements TrackableObject {
    private final Stack<T> history = new Stack<>();
    private final Flow.Publisher<T> publisher = new SubmissionPublisher<>();

    private String name;
    private T value;

    /**
     * 创建一个新的 FieldData 实例。
     */
    public FieldData() {
        Flow.Subscriber<T> subscriber = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                history.push(item);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        publisher.subscribe(subscriber);
    }

    /**
     * 使用指定的名称创建一个新的 FieldData 实例。
     *
     * @param name 字段的名称。
     */
    public FieldData(String name) {
        this();
        this.name = name;
    }

    /**
     * 获取字段的名称。
     *
     * @return 字段的名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 获取字段的当前值。
     *
     * @return 字段的当前值。
     */
    public T getValue() {
        return value;
    }

    /**
     * 设置字段的值，并将其添加到历史记录中。
     *
     * @param value 要设置的新值。
     */
    public void setValue(T value) {
        if (this.value == null && value == null) {
            return;
        }

        if (Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        ((SubmissionPublisher<T>) publisher).submit(this.value);
    }

    /**
     * 将字段标记为未更改，清除历史记录。
     */
    public void markAsUnchanged() {
        history.clear();
    }

    /**
     * 撤销字段的最后一次更改。
     */
    public void undo() {
        if (!history.isEmpty()) {
            this.value = history.pop();
        }
    }

    /**
     * 检查字段是否已更改。
     *
     * @return 如果字段已更改，则返回 true；否则返回 false。
     */
    @Override
    public boolean isChanged() {
        return !history.isEmpty();
    }

    /**
     * 检查字段是否已删除。
     *
     * @return 如果字段已删除，则返回 true；否则返回 false。
     */
    @Override
    public boolean isDeleted() {
        if (value instanceof TrackableObject trackableObject) {
            return trackableObject.isDeleted();
        }
        return false;
    }

    /**
     * 检查字段是否为新创建的对象。
     *
     * @return 如果字段为新创建的对象，则返回 true；否则返回 false。
     */
    @Override
    public boolean isNew() {
        if (value instanceof TrackableObject trackableObject) {
            return trackableObject.isNew();
        }
        return false;
    }

    /**
     * 检查字段是否可保存。
     *
     * @return 如果字段可保存，则返回 true；否则返回 false。
     */
    @Override
    public boolean isSavable() {
        if (value instanceof TrackableObject trackableObject) {
            return trackableObject.isSavable();
        }
        return false;
    }

    /**
     * 检查字段是否有效。
     *
     * @return 如果字段有效，则返回 true；否则返回 false。
     */
    @Override
    public boolean isValid() {
        if (value instanceof TrackableObject trackableObject) {
            return trackableObject.isValid();
        }
        return false;
    }

    /**
     * 检查字段是否忙碌。
     *
     * @return 如果字段忙碌，则返回 true；否则返回 false。
     */
    @Override
    public boolean isBusy() {
        if (value instanceof TrackableObject trackableObject) {
            return trackableObject.isBusy();
        }
        return false;
    }
}
