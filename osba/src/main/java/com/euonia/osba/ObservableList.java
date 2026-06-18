package com.euonia.osba;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.euonia.osba.abstracts.TrackableObject;

/**
 * 可观察列表，提供子属性通知、集合变更通知以及聚合忙碌状态通知。
 *
 * @param <TItem> 列表项类型
 * @author damon(zhaorong@outlook.com)
 */
public class ObservableList<TItem> extends ArrayList<TItem> {
    private final List<Consumer<ObjectChangedEventArgs>> childChangedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<ObjectChangedEventArgs>> itemPropertyChangedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<ObjectChangedEventArgs>> collectionChangedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Boolean>> busyChangedListeners = new CopyOnWriteArrayList<>();
    private final Map<TItem, PropertyChangeListener> propertyChangeListeners = new IdentityHashMap<>();
    private final Map<TItem, Consumer<Boolean>> busyListeners = new IdentityHashMap<>();

    private boolean raiseListChangedEvents = true;

    /**
     * 创建一个新的 ObservableList 实例，初始为空。
     */
    public ObservableList() {
        super();
    }

    /**
     * 创建一个新的 ObservableList 实例，并使用指定的集合初始化列表。
     *
     * @param items 初始化列表的集合
     */
    public ObservableList(Collection<? extends TItem> items) {
        super(items);
        for (TItem item : items) {
            hookItem(item);
        }
    }

    /**
     * 获取一个值，指示是否在列表项属性更改或集合结构更改时引发事件。
     *
     * @return 如果在列表项属性更改或集合结构更改时引发事件，则返回 true；否则返回 false。
     */
    public boolean isRaiseListChangedEvents() {
        return raiseListChangedEvents;
    }

    /**
     * 设置一个值，指示是否在列表项属性更改或集合结构更改时引发事件。
     *
     * @param raiseListChangedEvents 如果在列表项属性更改或集合结构更改时引发事件，则为 true；否则为 false。
     */
    public void setRaiseListChangedEvents(boolean raiseListChangedEvents) {
        this.raiseListChangedEvents = raiseListChangedEvents;
    }

    /**
     * 订阅子属性变更事件。当列表项的属性发生更改时，将触发此事件。
     *
     * @param listener 事件监听器
     */
    public void addChildChangedListener(Consumer<ObjectChangedEventArgs> listener) {
        if (listener == null) {
            return;
        }
        childChangedListeners.add(listener);
    }

    /**
     * 取消订阅子属性变更事件。
     *
     * @param listener 事件监听器
     */
    public void removeChildChangedListener(Consumer<ObjectChangedEventArgs> listener) {
        if (listener == null) {
            return;
        }
        childChangedListeners.remove(listener);
    }

    /**
     * 订阅列表中各项的属性变更事件。
     * 事件参数的 {@code propertyChangedEvent} 为非空，{@code collectionChangeType} 为空。
     */
    public void addItemPropertyChangedListener(Consumer<ObjectChangedEventArgs> listener) {
        if (listener == null) {
            return;
        }
        itemPropertyChangedListeners.add(listener);
    }

    /**
     * 取消订阅列表中各项的属性变更事件。
     *
     * @param listener 事件监听器
     */
    public void removeItemPropertyChangedListener(Consumer<ObjectChangedEventArgs> listener) {
        if (listener == null) {
            return;
        }
        itemPropertyChangedListeners.remove(listener);
    }

    /**
     * 订阅集合结构变更事件（添加、移除、替换、清空）。
     * 事件参数的 {@code collectionChangeType} 为非空，{@code propertyChangedEvent} 为空。
     *
     * @param listener 事件监听器
     */
    public void addCollectionChangedListener(Consumer<ObjectChangedEventArgs> listener) {
        if (listener == null) {
            return;
        }
        collectionChangedListeners.add(listener);
    }

    /**
     * 取消订阅集合结构变更事件。
     *
     * @param listener 事件监听器
     */
    public void removeCollectionChangedListener(Consumer<ObjectChangedEventArgs> listener) {
        if (listener == null) {
            return;
        }
        collectionChangedListeners.remove(listener);
    }

    /**
     * 订阅忙碌状态变更事件。
     *
     * @param listener 事件监听器
     */
    public void addBusyChangedListener(Consumer<Boolean> listener) {
        if (listener == null) {
            return;
        }
        busyChangedListeners.add(listener);
    }

    /**
     * 取消订阅忙碌状态变更事件。
     *
     * @param listener 事件监听器
     */
    public void removeBusyChangedListener(Consumer<Boolean> listener) {
        if (listener == null) {
            return;
        }
        busyChangedListeners.remove(listener);
    }

    /**
     * 获取一个值，指示列表中是否有任何项处于忙碌状态。如果列表中的任何项是 TrackableObject 并且其 IsBusy 属性为 true，则返回 true；否则返回 false。
     *
     * @return 如果列表中有任何项处于忙碌状态，则返回 true；否则返回 false。
     */
    public boolean isBusy() {
        for (TItem item : this) {
            if (item instanceof TrackableObject trackableObject && trackableObject.isBusy()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在一个范围内暂时禁止引发列表项属性更改和集合结构更改事件。当返回的 AutoCloseable 对象被关闭时，将恢复事件引发。
     *
     * @return 一个 AutoCloseable 对象，用于在关闭时恢复事件引发。
     */
    public AutoCloseable suppressListChangedEvents() {
        return new SuppressListChangedEventsScope();
    }

    /**
     * 将指定的元素添加到此列表的末尾，并引发相应的事件。
     *
     * @param item 要添加到列表末尾的元素
     * @return 如果列表因调用而发生更改，则返回 true；否则返回 false。
     */
    @Override
    public boolean add(TItem item) {
        var result = super.add(item);
        if (result) {
            hookItem(item);
            raiseCollectionChanged(item, ObjectChangedEventArgs.CollectionChangeType.ADD, size() - 1);
            raiseBusyChanged();
        }
        return result;
    }

    /**
     * 将指定的元素插入此列表中的指定位置，并引发相应的事件。
     *
     * @param index 要插入元素的位置
     * @param item  要插入的元素
     */
    @Override
    public void add(int index, TItem item) {
        super.add(index, item);
        hookItem(item);
        raiseCollectionChanged(item, ObjectChangedEventArgs.CollectionChangeType.ADD, index);
        raiseBusyChanged();
    }

    /**
     * 将指定的集合中的所有元素添加到此列表的末尾，并引发相应的事件。
     *
     * @param items 要添加到列表末尾的元素集合
     * @return 如果列表因调用而发生更改，则返回 true；否则返回 false。
     */
    @Override
    public boolean addAll(Collection<? extends TItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        var index = size();
        var result = super.addAll(items);
        if (result) {
            for (TItem item : items) {
                hookItem(item);
                raiseCollectionChanged(item, ObjectChangedEventArgs.CollectionChangeType.ADD, index++);
            }
            raiseBusyChanged();
        }
        return result;
    }

    /**
     * 将指定的集合中的所有元素插入此列表的指定位置，并引发相应的事件。
     *
     * @param index 要插入元素的位置
     * @param items 要插入的元素集合
     * @return 如果列表因调用而发生更改，则返回 true；否则返回 false。
     */
    @Override
    public boolean addAll(int index, Collection<? extends TItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        var result = super.addAll(index, items);
        if (result) {
            var currentIndex = index;
            for (TItem item : items) {
                hookItem(item);
                raiseCollectionChanged(item, ObjectChangedEventArgs.CollectionChangeType.ADD, currentIndex++);
            }
            raiseBusyChanged();
        }
        return result;
    }

    /**
     * 从此列表中移除指定位置的元素，并引发相应的事件。
     *
     * @param index 要移除元素的位置
     * @return 被移除的元素
     */
    @Override
    public TItem remove(int index) {
        TItem removed = super.remove(index);
        unhookItem(removed);
        raiseCollectionChanged(removed, ObjectChangedEventArgs.CollectionChangeType.REMOVE, index);
        raiseBusyChanged();
        return removed;
    }

    /**
     * 从此列表中移除指定的元素，并引发相应的事件。
     *
     * @param item 要移除的元素
     * @return 如果列表因调用而发生更改，则返回 true；否则返回 false。
     */
    @Override
    public boolean remove(Object item) {
        int index = indexOf(item);
        if (index < 0) {
            return false;
        }

        TItem removed = super.remove(index);
        unhookItem(removed);
        raiseCollectionChanged(removed, ObjectChangedEventArgs.CollectionChangeType.REMOVE, index);
        raiseBusyChanged();
        return true;
    }

    /**
     * 用指定的元素替换此列表中指定位置的元素，并引发相应的事件。
     *
     * @param index 要替换元素的位置
     * @param item  要存放在指定位置的元素
     * @return 被替换的元素
     */
    @Override
    public TItem set(int index, TItem item) {
        TItem oldItem = super.set(index, item);
        unhookItem(oldItem);
        hookItem(item);
        raiseCollectionChanged(item, ObjectChangedEventArgs.CollectionChangeType.REPLACE, index);
        raiseBusyChanged();
        return oldItem;
    }

    /**
     * 从此列表中移除所有元素，并引发相应的事件。
     */
    @Override
    public void clear() {
        if (isEmpty()) {
            return;
        }

        for (TItem item : this) {
            unhookItem(item);
        }
        super.clear();
        raiseCollectionChanged(null, ObjectChangedEventArgs.CollectionChangeType.CLEAR, -1);
        raiseBusyChanged();
    }

    /**
     * 订阅列表项的属性变更事件和忙碌状态变更事件，以便在这些事件发生时引发相应的事件。
     *
     * @param item 要订阅的列表项
     */
    private void hookItem(TItem item) {
        if (item == null) {
            return;
        }

        if (item instanceof BusinessObject<?> businessObject) {
            PropertyChangeListener listener = event -> onItemPropertyChanged(item, event);
            propertyChangeListeners.put(item, listener);
            businessObject.addPropertyChangeListener(listener);
        }

        if (item instanceof ObservableObject<?> observableObject) {
            Consumer<Boolean> listener = busy -> onItemBusyChanged();
            busyListeners.put(item, listener);
            observableObject.addBusyChangedListener(listener);
        }
    }

    /**
     * 取消订阅列表项的属性变更事件和忙碌状态变更事件，以避免在这些事件发生时引发相应的事件。
     *
     * @param item 要取消订阅的列表项
     */
    private void unhookItem(TItem item) {
        if (item == null) {
            return;
        }

        if (item instanceof BusinessObject<?> businessObject) {
            PropertyChangeListener listener = propertyChangeListeners.remove(item);
            if (listener != null) {
                businessObject.removePropertyChangeListener(listener);
            }
        }

        if (item instanceof ObservableObject<?> observableObject) {
            Consumer<Boolean> listener = busyListeners.remove(item);
            if (listener != null) {
                observableObject.removeBusyChangedListener(listener);
            }
        }
    }

    /**
     * 引发子属性变更事件，通知所有订阅的监听器列表项的属性已更改。
     *
     * @param args 子属性变更事件的参数
     */
    protected void onChildChanged(ObjectChangedEventArgs args) {
        for (Consumer<ObjectChangedEventArgs> listener : childChangedListeners) {
            listener.accept(args);
        }
    }

    /**
     * 引发忙碌状态变更事件，通知所有订阅的监听器列表的忙碌状态已更改。
     *
     * @param busy 忙碌状态的值
     */
    protected void onBusyChanged(boolean busy) {
        for (Consumer<Boolean> listener : busyChangedListeners) {
            listener.accept(busy);
        }
    }

    /**
     * 处理列表项的属性变更事件，并引发子属性变更事件和列表项属性变更事件，通知所有订阅的监听器列表项的属性已更改。
     *
     * @param item  发生属性变更的列表项
     * @param event 属性变更事件
     */
    private void onItemPropertyChanged(TItem item, PropertyChangeEvent event) {
        var args = new ObjectChangedEventArgs(item, event, null, -1);
        onChildChanged(args);
        for (Consumer<ObjectChangedEventArgs> listener : itemPropertyChangedListeners) {
            listener.accept(args);
        }
    }

    /**
     * 处理列表项的忙碌状态变更事件，并引发忙碌状态变更事件，通知所有订阅的监听器列表的忙碌状态已更改。
     */
    private void onItemBusyChanged() {
        raiseBusyChanged();
    }

    /**
     * 引发集合结构变更事件，通知所有订阅的监听器列表的集合结构已更改。
     *
     * @param item       发生变更的列表项
     * @param changeType 变更类型
     * @param index      变更的索引
     */
    private void raiseCollectionChanged(TItem item, ObjectChangedEventArgs.CollectionChangeType changeType, int index) {
        if (!raiseListChangedEvents) {
            return;
        }
        var args = new ObjectChangedEventArgs(item, null, changeType, index);
        onChildChanged(args);
        for (Consumer<ObjectChangedEventArgs> listener : collectionChangedListeners) {
            listener.accept(args);
        }
    }

    /**
     * 引发忙碌状态变更事件，通知所有订阅的监听器列表的忙碌状态已更改。
     */
    private void raiseBusyChanged() {
        onBusyChanged(isBusy());
    }

    /**
     * 表示一个范围，在该范围内暂时禁止引发列表项属性更改和集合结构更改事件。当此对象被关闭时，将恢复事件引发。
     */
    private final class SuppressListChangedEventsScope implements AutoCloseable {
        private final boolean previousValue;

        private SuppressListChangedEventsScope() {
            this.previousValue = raiseListChangedEvents;
            raiseListChangedEvents = false;
        }

        @Override
        public void close() {
            raiseListChangedEvents = previousValue;
        }
    }
}
