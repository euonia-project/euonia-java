package com.euonia.domain;

/**
 * {@link Entity} 接口表示领域模型中的通用实体。定义了所有实体应具备的基本属性和方法。
 * 实体是具有独特标识的对象，可以根据其标识而非属性与其他对象区分开来。它们通常表示系统中的现实世界概念或对象，常用于建模业务实体或领域对象。
 *
 * @param <ID> 实体标识符的类型，必须可比较
 * @author damon(zhaorong@outlook.com)
 */
public interface Entity<ID extends Comparable<ID>> {

    /**
     * 获取实体的唯一标识符，用于区分该实体与其他实体。
     *
     * @return 实体的唯一标识符
     */
    ID getId();

    /**
     * 设置实体的唯一标识符，用于区分该实体与其他实体。
     *
     * @param id 要为此实体设置的唯一标识符
     */
    void setId(ID id);

    /**
     * 获取唯一标识此实体的键数组，默认返回包含实体标识符的数组。
     *
     * @return 唯一标识此实体的键对象数组
     */
    default Object[] getKeys() {
        return new Object[]{getId()};
    }
}
