package com.euonia.uow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 工作单元模块的 Spring 自动配置。
 *
 * <p>注册以下 Bean：
 * <ul>
 *   <li>{@link UnitOfWorkAccessor} —— 用于持有当前环境工作单元的线程本地持有者</li>
 *   <li>{@link UnitOfWorkManager} —— 创建和管理工作单元的入口点</li>
 *   <li>{@link UnitOfWorkAspect} —— 包装标注了 {@code @UnitOfWork} 方法的 AOP 切面</li>
 * </ul>
 *
 * <p>启用 AspectJ 自动代理，使 {@link UnitOfWorkAspect} 可以拦截标注了注解的 Spring Bean。</p>
 *
 * <h3>使用方式</h3>
 * <p>对于 Spring Boot，此配置通过 {@code spring.factories} 自动检测。
 * 对于普通 Spring，手动导入：</p>
 * <pre>{@code
 * @Configuration
 * @Import(UnitOfWorkAutoConfiguration.class)
 * public class AppConfig { }
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 * @see UnitOfWorkAspect
 * @see UnitOfWorkManager
 */
@Configuration
@EnableAspectJAutoProxy
public class UnitOfWorkAutoConfiguration {

    /**
     * 创建用于跟踪当前环境工作单元的线程本地持有者。
     *
     * @return 新的 {@link UnitOfWorkAccessor}
     */
    @Bean
    public UnitOfWorkAccessor unitOfWorkAccessor() {
        return new UnitOfWorkAccessor();
    }

    /**
     * 创建工作单元管理器。
     *
     * @param accessor 线程本地访问器
     * @return 新的 {@link UnitOfWorkManager}
     */
    @Bean
    public UnitOfWorkManager unitOfWorkManager(UnitOfWorkAccessor accessor) {
        return new UnitOfWorkManager(accessor, new UnitOfWorkOptions());
    }

    /**
     * 创建 AOP 切面，将标注了 {@code @UnitOfWork} 的方法包装在工作单元中。
     *
     * @param manager 工作单元管理器
     * @return 新的 {@link UnitOfWorkAspect}
     */
    @Bean
    public UnitOfWorkAspect unitOfWorkAspect(UnitOfWorkManager manager) {
        return new UnitOfWorkAspect(manager);
    }
}
