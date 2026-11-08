package com.euonia.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.euonia.reflection.HasLazyServiceProvider;
import com.euonia.reflection.ServiceProvider;

/**
 * 为实现了 {@link HasLazyServiceProvider} 的 Bean 自动注入 {@link ServiceProvider}。
 * <p>
 * 对应 .NET 版中依赖注入容器对 {@code IHasLazyServiceProvider} 的属性注入行为。
 * 仅当 Bean 尚未设置服务提供者时注入，以便构造函数注入优先。
 *
 * @author damon(zhaorong@outlook.com)
 */
public class ApplicationServiceBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HasLazyServiceProvider holder && holder.getLazyServiceProvider() == null) {
            var provider = resolveServiceProvider();
            if (provider != null) {
                holder.setLazyServiceProvider(provider);
            }
        }
        return bean;
    }

    private ServiceProvider resolveServiceProvider() {
        var beanNames = applicationContext.getBeanNamesForType(ServiceProvider.class);
        if (beanNames.length == 0) {
            return null;
        }
        return applicationContext.getBean(beanNames[0], ServiceProvider.class);
    }
}
