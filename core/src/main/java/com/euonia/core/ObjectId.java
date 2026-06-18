package com.euonia.core;

import java.util.UUID;

/**
 * ObjectId 是一个表示对象唯一标识符的类。
 * 可以使用不同的算法生成，如 Snowflake、GUID、Random 和 ULID。
 * ObjectId 的值可以是 long、String、UUID 或 Integer 类型。
 * 该类提供了使用不同算法生成 ObjectId 的方法，
 * 并重写了 hashCode、equals 和 toString 方法以确保正确的功能。
 * ObjectId 类是不可变的，这意味着一旦创建，其值就不能更改。
 * 这确保了标识符的唯一性在其整个生命周期中得到维护。
 *
 * @author <a href="mailto:zhaorong@outlook.com>">damon(zhaorong@outlook.com)</a>
 */
public final class ObjectId {
    private final Object value;

    /**
     * 获取 ObjectId 的值。
     *
     * @return ObjectId 的值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 使用指定的值构造 ObjectId。
     *
     * @param value ObjectId 的值
     */
    public ObjectId(long value) {
        this.value = value;
    }

    /**
     * 使用指定的值构造 ObjectId。
     *
     * @param value ObjectId 的值
     */
    public ObjectId(String value) {
        this.value = value;
    }

    /**
     * 使用指定的值构造 ObjectId。
     *
     * @param value ObjectId 的值
     */
    public ObjectId(UUID value) {
        this.value = value;
    }

    /**
     * 使用指定的值构造 ObjectId。
     *
     * @param value ObjectId 的值
     */
    public ObjectId(Integer value) {
        this.value = value;
    }

    /**
     * 使用 Snowflake 算法生成新的 ObjectId。
     *
     * @return 新的 ObjectId
     */
    public static ObjectId snowflake() {
        return new ObjectId(SnowflakeId.getInstance().nextId());
    }

    /**
     * 使用 GUID 算法生成新的 ObjectId。
     *
     * @return 新的 ObjectId
     */
    public static ObjectId guid() {
        return guid(GuidType.DEFAULT);
    }

    /**
     * 使用 GUID 算法生成新的 ObjectId。
     *
     * @param type GUID 生成策略
     * @return 新的 ObjectId
     */
    public static ObjectId guid(GuidType type) {
        return new ObjectId(GuidGenerator.generate(type));
    }

    /**
     * 使用 Random 算法生成新的 ObjectId。
     *
     * @return 新的 ObjectId
     */
    public static ObjectId random() {
        return new ObjectId(RandomId.generate(System.currentTimeMillis()));
    }

    /**
     * 使用 ULID 算法生成新的 ObjectId。
     *
     * @return 新的 ObjectId
     */
    public static ObjectId ulid() {
        return new ObjectId(ULID.generate());
    }

    /**
     * 生成新的随机字符串 ID。
     *
     * @param seed 种子值
     * @return 新的随机字符串 ID
     */
    public static String newRandomId(long seed) {
        return RandomId.generate(seed);
    }

    /**
     * 使用当前时间作为种子生成新的随机字符串 ID。
     *
     * @return 新的随机字符串 ID
     */
    public static String newRandomId() {
        return newRandomId(System.currentTimeMillis());
    }

    /**
     * 生成新的 Snowflake ID。
     *
     * @return 新的 Snowflake ID
     */
    public static long newSnowflakeId() {
        return SnowflakeId.getInstance().nextId();
    }

    /**
     * 使用默认生成策略（SIMPLE）生成新的 GUID。
     *
     * @return 新的 GUID
     */
    public static UUID newGuid() {
        return GuidGenerator.generate(GuidType.DEFAULT);
    }

    /**
     * 根据指定的类型生成新的 GUID。
     *
     * @param type GUID 生成策略
     * @return 新的 GUID
     */
    public static UUID newGuid(GuidType type) {
        return GuidGenerator.generate(type);
    }

    /**
     * 生成新的 ULID 字符串。
     *
     * @return 新的 ULID 字符串
     */
    public static String newUlid() {
        return ULID.generate();
    }

    public <T> T getValue(Class<T> type) {
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ObjectId objectId = (ObjectId) obj;
        return value.equals(objectId.value);
    }

    @Override
    public String toString() {

        if (value instanceof UUID target) {
            return target.toString();
        }

        if (value instanceof Integer target) {
            return target.toString();
        }

        if (value instanceof Long target) {
            return target.toString();
        }

        if (value instanceof String target) {
            return target;
        }

        if (value instanceof ULID target) {
            return target.toString();
        }

        return value.toString();
    }
}
