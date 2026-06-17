package com.euonia.osba;

import java.util.Objects;
import java.util.function.Function;

import com.euonia.factory.ObjectFactory;

/**
 * 业务上下文，为 OSBA 工厂创建的业务对象提供轻量级的服务解析和实例创建网关。
 *
 * @author damon(zhaorong@outlook)
 */
public final class BusinessContext {
    private final Function<Class<?>, ?> instanceCreator;
    private final ObjectFactory objectFactory;

    /**
     * 使用指定的实例创建函数和 ObjectFactory 创建 BusinessContext 的新实例。
     *
     * @param instanceCreator 用于创建指定类型实例的函数。
     * @param objectFactory   此 BusinessContext 使用的 ObjectFactory。
     */
    public BusinessContext(Function<Class<?>, ?> instanceCreator, ObjectFactory objectFactory) {
        this.instanceCreator = instanceCreator;
        this.objectFactory = objectFactory;
    }

    /**
     * 从上下文中获取指定类型的实例，如果不存在则创建。
     *
     * @param objectType 要获取或创建的对象的类型。
     * @param <T>        对象的类型。
     * @return 指定类型的实例。
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreateObject(Class<T> objectType) {
        Objects.requireNonNull(objectType, "objectType");
        if (instanceCreator == null) {
            throw new IllegalStateException("BusinessContext 不支持实例创建。");
        }
        return (T) instanceCreator.apply(objectType);
    }

    /**
     * 获取与此 BusinessContext 关联的 ObjectFactory，该工厂可用于基于注解的工厂方法创建、获取、插入、更新、保存、执行和删除业务对象。
     *
     * @return 与此 BusinessContext 关联的 ObjectFactory。
     */
    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }
}
