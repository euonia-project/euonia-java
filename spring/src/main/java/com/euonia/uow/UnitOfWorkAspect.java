package com.euonia.uow;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Spring AOP 切面，拦截标注了
 * {@link com.euonia.uow.annotation.UnitOfWork @UnitOfWork} 的方法，
 * 并将其包装在工作单元中。
 *
 * <h3>工作原理</h3>
 * <ol>
 *   <li>方法执行前，通过 {@link UnitOfWorkManager} 开启新的 {@link UnitOfWork}。</li>
 *   <li>方法正常执行。</li>
 *   <li>成功后，完成工作单元（保存 → 处理器 → 监听器）。</li>
 *   <li>失败后，回滚工作单元并传播异常。通过 {@link UnitOfWork#close()} 触发失败监听器。</li>
 *   <li>工作单元始终在 {@code finally} 块中释放。</li>
 * </ol>
 *
 * <h3>切入点</h3>
 * <p>拦截类或方法上标注了 {@code @UnitOfWork} 的任意 Spring 管理的 Bean 方法。
 * 标注了 {@code @UnitOfWork(disabled = true)} 的方法会被跳过。</p>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWorkManager
 * @see com.euonia.uow.annotation.UnitOfWork
 */
@Aspect
public class UnitOfWorkAspect {

    private final UnitOfWorkManager unitOfWorkManager;

    /**
     * 创建使用给定管理器进行工作单元生命周期管理的切面。
     *
     * @param unitOfWorkManager 工作单元管理器
     */
    public UnitOfWorkAspect(UnitOfWorkManager unitOfWorkManager) {
        this.unitOfWorkManager = unitOfWorkManager;
    }

    /**
     * 环绕通知，将标注了注解的方法包装在工作单元中。
     *
     * @param pjp 连接点
     * @return 方法的返回值
     * @throws Throwable 如果方法抛出异常
     */
    @Around("@within(com.euonia.uow.annotation.UnitOfWork) || @annotation(com.euonia.uow.annotation.UnitOfWork)")
    public Object aroundUnitOfWork(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        if (!UnitOfWorkHelper.isUnitOfWorkMethod(method)) {
            return pjp.proceed();
        }

        try (UnitOfWork uow = unitOfWorkManager.begin(new UnitOfWorkOptions(true), false)) {
            Object result = pjp.proceed();
            uow.completeAsync().toCompletableFuture().join();
            return result;
        } catch (Throwable t) {
            // close()（通过 try-with-resources）检测 !completed
            // 并自动触发失败监听器
            throw t;
        }
    }
}
