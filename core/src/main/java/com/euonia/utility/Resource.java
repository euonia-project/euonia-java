package com.euonia.utility;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Resource 类提供了从资源文件中获取字符串值的静态方法，并支持可选的参数格式化。
 * <p>
 * 该类主要用于国际化和本地化场景，通过指定资源文件的基本名称和键来获取对应的字符串值。如果提供了参数，则会使用 String.format 方法对字符串进行格式化。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * String message = Resource.getString("messages", "greeting", "John");
 * </pre>
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class Resource {

    private Resource() {
    }

    /**
     * 获取指定资源文件中指定键的字符串值，并根据提供的参数进行格式化。
     *
     * @param baseName 资源文件的基本名称（不包含扩展名和路径）
     * @param key      资源文件中对应的键
     * @param args     可选的参数，用于格式化字符串
     * @return 格式化后的字符串值，如果没有提供参数，则返回原始字符串值
     */
    public static String getString(String baseName, String key, Object... args) {
        return getString(baseName, key, Locale.getDefault(), args);
    }

    /**
     * 获取指定资源文件中指定键的字符串值，并根据提供的参数进行格式化。
     *
     * @param baseName 资源文件的基本名称（不包含扩展名和路径）
     * @param key      资源文件中对应的键
     * @param locale   指定的区域设置
     * @param args     可选的参数，用于格式化字符串
     * @return 格式化后的字符串值，如果没有提供参数，则返回原始字符串值
     */
    public static String getString(String baseName, String key, Locale locale, Object... args) {
        var caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                                .getCallerClass();
        ResourceBundle resource = ResourceBundle.getBundle(baseName, locale, caller.getClassLoader());
        var value = resource.getString(key);
        if (args != null && args.length > 0) {
            return String.format(value, args);
        } else {
            return value;
        }
    }
}
