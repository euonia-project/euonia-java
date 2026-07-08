package com.euonia.domain.auditing;

import java.time.Instant;

import com.euonia.domain.EntityBase;

/**
 * 审计记录实体，用于记录对领域实体的操作审计信息。
 * <p>
 * 包含被审计实体的名称、ID、操作类型、时间戳以及执行操作的用户信息。
 * 继承自 {@link EntityBase}，支持自定义 ID 类型。
 *
 * @param <ID> 实体 ID 的类型
 * @author damon(zhaorong@outlook.com)
 */
public class AuditRecord<ID extends Comparable<ID>> extends EntityBase<ID> {
    /**
     * 被审计的实体名称
     */
    private final String entityName;
    /**
     * 被审计实体的 ID
     */
    private final String entityId;
    /**
     * 执行的操作类型（如 CREATE、UPDATE、DELETE）
     */
    private final String action;
    /**
     * 操作发生的时间戳
     */
    private final Instant timestamp;
    /**
     * 审计备注
     */
    private String comment;
    /**
     * 执行操作的用户 ID
     */
    private String userId;
    /**
     * 执行操作的用户名
     */
    private String userName;

    /**
     * 使用指定的实体名称、实体 ID、操作类型和时间戳构造审计记录。
     *
     * @param entityName 实体名称
     * @param entityId   实体 ID
     * @param action     操作类型
     * @param timestamp  操作时间戳
     */
    public AuditRecord(String entityName, String entityId, String action, Instant timestamp) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.timestamp = timestamp;
    }

    /**
     * 使用指定的实体名称、实体 ID 和操作类型构造审计记录，时间戳自动设为当前时间。
     *
     * @param entityName 实体名称
     * @param entityId   实体 ID
     * @param action     操作类型
     */
    public AuditRecord(String entityName, String entityId, String action) {
        this(entityName, entityId, action, Instant.now());
    }

    /**
     * 使用实体类、实体 ID 和操作类型构造审计记录，实体名称从类的简单名称获取。
     *
     * @param entityClass 实体类
     * @param entityId    实体 ID
     * @param action      操作类型
     */
    public AuditRecord(Class<?> entityClass, String entityId, String action) {
        this(entityClass.getSimpleName(), entityId, action, Instant.now());
    }

    /**
     * 获取被审计的实体名称。
     *
     * @return 实体名称
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * 获取被审计实体的 ID。
     *
     * @return 实体 ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * 获取操作类型。
     *
     * @return 操作类型
     */
    public String getAction() {
        return action;
    }

    /**
     * 获取操作时间戳。
     *
     * @return 操作时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * 获取审计备注。
     *
     * @return 审计备注
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置审计备注。
     *
     * @param comment 审计备注
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取执行操作的用户 ID。
     *
     * @return 用户 ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置执行操作的用户 ID。
     *
     * @param userId 用户 ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取执行操作的用户名。
     *
     * @return 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置执行操作的用户名。
     *
     * @param userName 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
