package com.euonia.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于为字段指定显示名称的注解，可在 UI 或错误消息中使用以替代实际的字段名。
 * 这对于为代码中可能具有技术性或不具描述性名称的字段提供更友好的名称特别有用。
 *
 * @author damon(zhaorong@outlook)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface DisplayName {
    /**
     * 获取显示名称的值。
     *
     * @return 显示名称的值
     */
    String value();
}
