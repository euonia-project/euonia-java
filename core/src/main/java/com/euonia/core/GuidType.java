package com.euonia.core;

/**
 * 描述要生成的 GUID 值的类型。
 *
 * @author damon(zhaorong@outlook.com)
 */
public enum GuidType {
    /**
     * 表示空的 GUID。
     */
    EMPTY,
    /**
     * 表示按默认方式生成 GUID。
     */
    DEFAULT,
    /**
     * 表示按字符串顺序生成 GUID。
     */
    SEQUENTIAL_AS_STRING,
    /**
     * 表示按二进制顺序生成 GUID。
     */
    SEQUENTIAL_AS_BINARY,
    /**
     * 表示在末尾按顺序生成 GUID。
     */
    SEQUENTIAL_AT_END
}
