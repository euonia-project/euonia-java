package com.euonia.domain.auditing;

/**
 * 审计记录存储接口。实现类可以选择将记录存储到数据库、文件系统或任何其他存储机制中。
 * <p>
 * {@code save} 方法是泛型的，可以处理任何继承自 {@link AuditRecord} 的审计记录类型，从而灵活支持不同类型的记录。ID 类型也是泛型的，允许使用不同类型的标识符（如 Long、String、UUID 等）。
 *
 * @author damon(zhaorong@outlook.com)
 */
public interface AuditStore {
    /**
     * 保存审计记录。
     *
     * @param <T>    审计记录类型，必须继承自 {@link AuditRecord}
     * @param <ID>   审计记录 ID 的类型
     * @param record 要保存的审计记录
     */
    <T extends AuditRecord<ID>, ID extends Comparable<ID>> void save(T record);
}
