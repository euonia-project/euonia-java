package com.euonia.osba.rules;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.euonia.reflection.PropertyInfo;

/**
 * 表示一个基于特定数据注解的规则。该规则可用于根据属性上指定注解的存在和配置来强制执行验证或其他逻辑。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface Rule {
    /**
     * 获取规则的名称，该名称是规则的唯一标识符。该名称可用于在各种上下文中引用规则，
     * 例如在将规则应用于业务对象时或记录规则执行时。
     *
     * @return 规则的名称，即规则的唯一标识符
     */
    String getName();

    /**
     * 获取与此规则关联的属性，该属性是规则设计用于评估或强制执行的主要属性。
     * 此属性是规则逻辑的主要焦点，可用于确定何时应用规则以及如何评估规则。
     *
     * @return 与此规则关联的属性，即规则设计用于评估或强制执行的主要属性
     */
    PropertyInfo<?> getProperty();

    /**
     * 获取与此规则关联的相关属性列表。可用于识别在评估规则时可能相关的其他属性，
     * 从而实现更复杂和相互关联的规则逻辑。
     *
     * @return 与此规则关联的相关属性列表，可用于规则逻辑的评估
     */
    default List<PropertyInfo<?>> getRelatedProperties() {
        return List.of();
    }

    /**
     * 获取规则的优先级，可用于确定多个规则应用时的执行顺序。
     * 优先级较高的规则将在优先级较低的规则之前执行。
     *
     * @return 规则的优先级，值越高表示优先级越高
     */
    int getPriority();

    /**
     * 异步执行规则，根据提供的上下文执行必要的检查或操作。此方法的实现应包含评估规则的逻辑，
     * 并在规则被违反时可能修改上下文或抛出异常。
     *
     * @param context 执行规则的上下文，包含规则评估的相关信息和状态
     * @return 一个在规则执行完成时完成的 CompletableFuture，允许异步处理以及处理规则对上下文的影响
     */
    CompletableFuture<Void> executeAsync(RuleContext context);
}
