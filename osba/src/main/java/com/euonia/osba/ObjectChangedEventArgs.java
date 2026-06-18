package com.euonia.osba;

import java.beans.PropertyChangeEvent;

/**
 * 表示业务对象属性更改或集合更改事件的参数。该类包含有关更改的对象、属性更改事件、集合更改类型和集合索引的信息。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ObjectChangedEventArgs {
    private final Object changedObject;
    private final PropertyChangeEvent propertyChangedEvent;
    private final CollectionChangeType collectionChangeType;
    private final int collectionIndex;

    public ObjectChangedEventArgs(Object changedObject) {
        this(changedObject, null, null, -1);
    }

    public ObjectChangedEventArgs(Object changedObject,
                                  PropertyChangeEvent propertyChangedEvent,
                                  CollectionChangeType collectionChangeType,
                                  int collectionIndex) {
        this.changedObject = changedObject;
        this.propertyChangedEvent = propertyChangedEvent;
        this.collectionChangeType = collectionChangeType;
        this.collectionIndex = collectionIndex;
    }

    public Object getChangedObject() {
        return changedObject;
    }

    public PropertyChangeEvent getPropertyChangedEvent() {
        return propertyChangedEvent;
    }

    public CollectionChangeType getCollectionChangeType() {
        return collectionChangeType;
    }

    public int getCollectionIndex() {
        return collectionIndex;
    }

    public enum CollectionChangeType {
        ADD,
        REMOVE,
        REPLACE,
        CLEAR
    }
}
