package com.euonia.application;

import com.euonia.reflection.HasLazyServiceProvider;

/**
 * ApplicationService 是一个标记接口，表示实现该接口的类是应用服务。
 * 应用服务通常用于处理应用程序的业务逻辑，协调领域对象和基础设施服务之间的交互。
 * <p>
 * 与 .NET 版 {@code IApplicationService} 一致，实现类型同时需要暴露
 * {@link HasLazyServiceProvider#getLazyServiceProvider() 延迟服务提供者}，
 * 以便依赖注入容器在初始化阶段完成注入。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface ApplicationService extends HasLazyServiceProvider {
}
