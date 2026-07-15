package com.euonia.domain.command;

import java.util.HashMap;
import java.util.Map;

import com.euonia.reflection.TypeHelper;

/**
 * 命令基类，实现 {@link Command} 接口并提供键值对属性存储功能。
 * <p>
 * 命令的属性以字符串形式存储，支持通过 {@link #get(String, Class)} 方法进行类型安全的属性值转换。具体的命令类应继承此基类。
 *
 * @author damon(zhaorong@outlook.com)
 */
public abstract class CommandBase implements Command {

    /** 命令属性存储 */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * 获取所有命令属性。
     *
     * @return 属性映射
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * 根据键获取属性值，如果键不存在则返回 null。
     *
     * @param property 属性键
     * @return 属性值，如果键不存在则返回 null
     */
    public final String get(String property) {
        return properties.getOrDefault(property, null);
    }

    /**
     * 设置属性键值对。
     *
     * @param property 属性键
     * @param value    属性值
     */
    public final void set(String property, String value) {
        properties.put(property, value);
    }

    /**
     * 根据键获取属性值，并强制转换为指定类型。
     * <p>
     * 使用 {@link TypeHelper#coerceValue} 进行类型转换。
     *
     * @param <T>      期望的值类型
     * @param property 属性键
     * @param castType 目标类型
     * @return 强制转换后的属性值，如果键不存在则返回 null
     */
    public <T> T get(String property, Class<T> castType) {
        var value = properties.getOrDefault(property, null);
        if (value == null) {
            return null;
        }
        return TypeHelper.coerceValue(castType, value);
    }
}
