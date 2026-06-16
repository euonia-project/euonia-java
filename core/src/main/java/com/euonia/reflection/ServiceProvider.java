package com.euonia.reflection;

import java.util.List;
import java.util.Optional;

/**
 * ServiceProvider 是一个接口，定义了用于解析和创建服务实例的方法。
 * 此接口的实现可以提供自定义逻辑来检索和实例化服务。
 *
 * @author damon(zhaorong@outlook)
 */
public interface ServiceProvider {
    /**
     * 检索指定类型的服务实例。
     *
     * @param <T>  服务的类型
     * @param type 服务的类
     * @return 包含服务实例的 Optional，如果未找到则为空
     */
    <T> Optional<T> getService(Class<T> type);

    /**
     * 检索指定类型和泛型类型参数的服务实例。
     *
     * @param <T>                  服务的类型
     * @param type                 服务的类
     * @param genericTypeArguments 服务的泛型类型参数
     * @return 包含服务实例的 Optional，如果未找到则为空
     */
    <T> Optional<T> getService(Class<T> type, Class<?>... genericTypeArguments);

    /**
     * 检索指定类型和服务名称的服务实例。
     *
     * @param <T>         服务的类型
     * @param type        服务的类
     * @param serviceName 服务名称
     * @return 包含服务实例的 Optional，如果未找到则为空
     */
    <T> Optional<T> getService(Class<T> type, String serviceName);

    /**
     * 检索指定类型的必需服务实例。如果服务未找到，则抛出异常。
     *
     * @param <T>  服务的类型
     * @param type 服务的类
     * @return 服务实例
     * @throws IllegalStateException 如果服务未找到
     */
    <T> T getRequiredService(Class<T> type);

    /**
     * 检索指定类型的所有服务实例。
     *
     * @param <T>  服务的类型
     * @param type 服务的类
     * @return 服务实例列表
     */
    <T> List<T> getServices(Class<T> type);

    /**
     * 检索指定类型和泛型类型参数的所有服务实例。
     *
     * @param <T>                  服务的类型
     * @param type                 服务的类
     * @param genericTypeArguments 服务的泛型类型参数
     * @return 服务实例列表
     */
    <T> List<T> getServices(Class<T> type, Class<?>... genericTypeArguments);

    /**
     * 使用提供的构造函数参数创建指定类型的新实例。
     *
     * @param <T>                  服务的类型
     * @param type                 服务的类
     * @param constructorArguments 传递给构造函数的参数
     * @return 创建的实例
     * @throws IllegalStateException 如果无法创建实例
     */
    <T> T createInstance(Class<T> type, Object... constructorArguments);

    /**
     * 检索指定类型的服务实例，如果服务未找到则创建新实例。
     *
     * @param <T>                  服务的类型
     * @param type                 服务的类
     * @param constructorArguments 如果创建新实例则传递给构造函数的参数
     * @return 服务实例或新创建的实例
     */
    default <T> T getServiceOrCreate(Class<T> type, Object... constructorArguments) {
        return getService(type).orElseGet(() -> createInstance(type, constructorArguments));
    }
}
