package com.euonia.osba.abstracts;

import com.euonia.osba.BusinessContext;

/**
 * 标记一个参与 OSBA 业务上下文传播的对象。
 *
 * @author damon(zhaorong@outlook)
 */
public interface UseBusinessContext {
    /**
     * 获取当前对象的业务上下文。业务上下文包含了与当前业务操作相关的信息，如用户信息、环境信息等，可以在业务逻辑中使用这些信息来执行特定的操作或决策。
     *
     * @return 当前对象的业务上下文
     */
    BusinessContext getBusinessContext();

    /**
     * 设置当前对象的业务上下文。
     *
     * @param businessContext 要设置的业务上下文
     */
    void setBusinessContext(BusinessContext businessContext);
}
