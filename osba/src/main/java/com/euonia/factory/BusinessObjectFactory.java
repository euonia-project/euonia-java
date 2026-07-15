package com.euonia.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import com.euonia.core.ArgumentOutOfRangeException;
import com.euonia.core.PriorityValueFinder;
import com.euonia.factory.annotation.FactoryCreate;
import com.euonia.factory.annotation.FactoryDelete;
import com.euonia.factory.annotation.FactoryExecute;
import com.euonia.factory.annotation.FactoryFetch;
import com.euonia.factory.annotation.FactoryInsert;
import com.euonia.factory.annotation.FactoryUpdate;
import com.euonia.osba.BusinessContext;
import com.euonia.osba.ExecutableObject;
import com.euonia.osba.ObservableObject;
import com.euonia.osba.ReadOnlyObject;
import com.euonia.osba.abstracts.UseBusinessContext;
import com.euonia.reflection.ObjectReflector;
import com.euonia.reflection.ServiceProvider;
import com.euonia.utility.Resource;

/**
 * BusinessObjectFactory 是 ObjectFactory 接口的实现，
 * 使用反射基于带注解的工厂方法来创建、检索、插入、更新、保存、执行和删除业务对象。
 * 它支持与 bean 工厂集成以进行对象实例化，并根据保存操作处理不同的对象状态。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class BusinessObjectFactory implements ObjectFactory {

    private final ServiceProvider provider;

    /**
     * 使用指定的 ServiceProvider 构造一个新的 BusinessObjectFactory，
     * ServiceProvider 可用于在对象创建和方法调用期间解析依赖。
     *
     * @param provider 用于解析服务的 ServiceProvider
     */
    public BusinessObjectFactory(ServiceProvider provider) {
        this.provider = provider;
    }

    /**
     * 通过查找并调用标注了 @FactoryCreate 的适当工厂方法，
     * 使用提供的条件作为参数来创建指定类型的实例。
     *
     * @param type     要创建的对象的类。
     * @param criteria 用于创建对象的参数。
     * @param <T>      要创建的对象的类型。
     * @return 创建的对象。
     */
    @Override
    public <T> T create(Class<T> type, Object... criteria) {
        var method = ObjectReflector.findFactoryMethod(type, FactoryCreate.class, criteria);
        var target = getObjectInstance(type);
        if (target instanceof ObservableObject<?> editableObject) {
            editableObject.markAsNew();
        }

        invoke(method, target, criteria);
        return target;
    }

    /**
     * 通过查找并调用标注了 @FactoryFetch 的适当工厂方法，
     * 使用提供的条件作为参数来检索指定类型的实例。
     *
     * @param type     要检索的对象的类。
     * @param criteria 用于检索对象的参数。
     * @param <T>      要检索的对象的类型。
     * @return 检索到的对象。
     */
    @Override
    public <T> T fetch(Class<T> type, Object... criteria) {
        var method = ObjectReflector.findFactoryMethod(type, FactoryFetch.class, criteria);
        var target = getObjectInstance(type);
        invoke(method, target, criteria);
        return target;
    }

    /**
     * 通过查找并调用标注了 @FactoryInsert 的适当工厂方法，
     * 使用提供的条件作为参数来插入指定类型的新实例。
     *
     * @param type     要插入的对象的类。
     * @param criteria 用于插入对象的参数。
     * @param <T>      要插入的对象的类型。
     * @return 插入的对象。
     */
    @Override
    public <T> T insert(Class<T> type, Object... criteria) {
        var method = ObjectReflector.findFactoryMethod(type, FactoryInsert.class, criteria);
        var target = getObjectInstance(type);
        invoke(method, target, criteria);
        return target;
    }

    /**
     * 通过查找并调用标注了 @FactoryUpdate 的适当工厂方法，
     * 使用提供的条件作为参数来更新指定类型的现有实例。
     *
     * @param type     要更新的对象的类。
     * @param criteria 用于更新对象的参数。
     * @param <T>      要更新的对象的类型。
     * @return 更新后的对象。
     */
    @Override
    public <T> T update(Class<T> type, Object... criteria) {
        var method = ObjectReflector.findFactoryMethod(type, FactoryUpdate.class, criteria);
        var target = getObjectInstance(type);
        invoke(method, target, criteria);
        return target;
    }

    /**
     * 通过判断对象的状态并根据注解调用相应的工厂方法来保存提供的对象。
     *
     * @param type   要保存的对象的类。
     * @param target 要保存的对象实例。
     * @param <T>    要保存的对象的类型。
     * @return 保存后的对象。
     */
    @Override
    public <T> T save(Class<T> type, T target) {
        Method method;
        if (target instanceof ObservableObject<?> editableObject) {
            switch (editableObject.getState()) {
                case NEW -> method = ObjectReflector.findFactoryMethod(type, FactoryInsert.class, new Object[0]);
                case CHANGED -> method = ObjectReflector.findFactoryMethod(type, FactoryUpdate.class, new Object[0]);
                case DELETED -> method = ObjectReflector.findFactoryMethod(type, FactoryDelete.class, new Object[0]);
                default ->
                    throw new ArgumentOutOfRangeException(Resource.getString("resource", "BusinessObjectFactory.UnexpectedState", editableObject.getState(), type.getName()));
            }
        } else if (target instanceof ExecutableObject) {
            method = ObjectReflector.findFactoryMethod(type, FactoryExecute.class, new Object[0]);
        } else if (target instanceof ReadOnlyObject) {
            throw new UnsupportedOperationException(Resource.getString("resource", "BusinessObjectFactory.CannotSaveReadOnly", type.getName()));
        } else {
            method = ObjectReflector.findFactoryMethod(type, FactoryUpdate.class, new Object[0]);
        }
        invoke(method, target);
        return target;
    }

    /**
     * 通过查找并调用标注了 @FactoryExecute 的适当工厂方法，
     * 使用提供的条件作为参数来执行提供的对象。
     *
     * @param type     要执行的对象的类。
     * @param criteria 用于执行对象的参数。
     * @param <T>      要执行的对象的类型。
     * @return 执行后的对象。
     */
    @Override
    public <T> T execute(Class<T> type, Object... criteria) {
        var method = ObjectReflector.findFactoryMethod(type, FactoryExecute.class, criteria);
        var target = getObjectInstance(type);
        invoke(method, target, criteria);
        return target;
    }

    /**
     * 通过查找并调用标注了 @FactoryDelete 的适当工厂方法，
     * 使用提供的条件作为参数来删除指定的对象。
     *
     * @param type     要删除的对象的类。
     * @param criteria 用于删除对象的参数。
     * @param <T>      要删除的对象的类型。
     */
    @Override
    public <T> void delete(Class<T> type, Object... criteria) {
        var method = ObjectReflector.findFactoryMethod(type, FactoryDelete.class, criteria);
        var target = getObjectInstance(type);
        invoke(method, target, criteria);
    }

    /**
     * 如果可用，使用 bean 工厂创建指定类型的实例，否则回退到基于反射的实例化。
     * 如果创建的对象实现了 UseBusinessContext，则为其设置业务上下文。
     *
     * @param type 要创建的对象的类。
     * @param <T>  要创建的对象的类型。
     * @return 创建的对象实例。
     */
    @SuppressWarnings("unchecked")
    private <T> T getObjectInstance(Class<T> type) {
        T object = PriorityValueFinder.find(queue -> {
            if (provider != null) {
                queue.add(() -> {
                    try {
                        return provider.getService(type).orElse(null);
                    } catch (Exception e) {
                        return null;
                    }
                }, 1);
            }
            queue.add(() -> {
                try {
                    var constructors = Arrays.stream(type.getDeclaredConstructors())
                                             .sorted((a, b) -> Integer.compare(b.getParameterCount(), a.getParameterCount()))
                                             .toList();

                    var ctor = constructors.stream().findFirst().orElse(null);

                    if (ctor == null) {
                        throw new RuntimeException(Resource.getString("resource", "BusinessObjectFactory.NoConstructor", type.getName()));
                    }

                    var parameters = ctor.getParameters();
                    if (parameters.length == 0) {
                        return (T) ctor.newInstance();
                    } else {
                        var args = new Object[parameters.length];
                        for (int i = 0; i < parameters.length; i++) {
                            var parameterType = parameters[i].getType();
                            args[i] = getObjectInstance(parameterType);
                        }
                        return (T) ctor.newInstance(args);
                    }
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException e) {
                    throw new RuntimeException(Resource.getString("resource", "BusinessObjectFactory.CreateInstanceFailed", type.getName()), e);
                }
            }, 2);
        }, Objects::nonNull, null);

        if (object instanceof UseBusinessContext businessObject) {
            businessObject.setBusinessContext(new BusinessContext(this::getObjectInstance, this));
        }

        return object;
    }

    /**
     * 使用给定的条件作为参数，在目标对象上调用指定的方法，处理调用过程中可能发生的任何异常。
     *
     * @param method   要调用的方法。
     * @param target   要调用方法的目标对象。
     * @param criteria 用于调用方法的参数。
     * @param <T>      目标对象的类型。
     */
    private <T> void invoke(Method method, T target, Object... criteria) {
        try {
            method.setAccessible(true);
            method.invoke(target, criteria);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(Resource.getString("resource", "BusinessObjectFactory.InvokeMethodFailed", method.getName(), target.getClass().getName()), e);
        }
    }

}
