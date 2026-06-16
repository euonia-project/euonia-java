package com.euonia.utility;

import java.util.function.Supplier;

/**
 * StringUtility 是一个工具类，提供字符串操作的常用方法。
 * 旨在简化字符串处理并提高代码可读性。
 *
 * @author damon(zhaorong@outlook)
 */
public class StringUtility {
    /**
     * 将输入字符串的首字母大写。
     *
     * @param input 要处理的字符串
     * @return 首字母大写后的字符串
     */
    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * 将输入字符串的首字母小写。
     *
     * @param input 要处理的字符串
     * @return 首字母小写后的字符串
     */
    public static String decapitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    /**
     * 将输入字符串的首字母大写，并在单词之间添加下划线。
     *
     * @param input 要处理的字符串
     * @return 首字母大写并添加下划线后的字符串
     */
    public static String capitalizeFirstLetterWithUnderscore(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * collapse 方法接受一个字符串数组，并返回第一个非空且非空字符串的值。
     * 如果所有字符串都是空的或 null，则返回 null。
     *
     * @param parts 要处理的字符串数组
     * @return 第一个非空且非空字符串的值，如果所有字符串都是空的或 null，则返回 null
     */
    public static String collapse(String... parts) {
        for (String part : parts) {
            if (part != null && !part.isEmpty()) {
                return part;
            }
        }
        return null;
    }

    /**
     * collapse 方法接受一个字符串数组，并返回第一个非空且非空字符串的值。
     * 如果所有字符串都是空的或 null，则返回 null。
     *
     * @param suppliers 要处理的字符串供应者数组
     * @return 第一个非空且非空字符串的值，如果所有字符串都是空的或 null，则返回 null
     */
    @SafeVarargs
    public static String collapse(Supplier<String>... suppliers) {
        for (Supplier<String> supplier : suppliers) {
            String value = supplier.get();
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }
}
