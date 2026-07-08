package com.euonia.uow;

/**
 * 标记接口，表示某个类型需要工作单元支持。
 *
 * <p>在任何服务或处理器类上实现此接口，当与工作单元感知的代理或 AOP 切面配合使用时，
 * 其公共方法将被自动拦截并包装在工作单元中。</p>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWork
 */
public interface UnitOfWorkEnabled {
}
