package com.euonia.osba;

/**
 * Marks an object that participates in OSBA business-context propagation.
 */
public interface UseBusinessContext {
    BusinessContext getBusinessContext();

    void setBusinessContext(BusinessContext businessContext);
}
