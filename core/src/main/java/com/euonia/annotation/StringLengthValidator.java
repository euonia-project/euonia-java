package com.euonia.annotation;

import com.euonia.utility.StringUtility;

/**
 * StringLengthValidator 是一个用于验证字符串长度是否在指定范围内的验证器类。
 * 它实现了 Validator 接口，并提供了 validate 方法来执行验证逻辑。
 * <p>
 * 验证逻辑如下：
 * <ul>
 *     <li>如果值是一个字符串，则检查其长度是否在注解指定的最小值和最大值之间。</li>
 *     <li>如果长度不在指定范围内，则验证失败，并返回指定的错误消息或默认消息 "Value must be between {min} and {max} characters long"。</li>
 *     <li>如果值不是字符串，则验证成功。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class StringLengthValidator implements Validator<StringLength> {
    @Override
    public Result validate(StringLength annotation, Object value) {
        boolean result = true;
        String message = null;

        if (value instanceof String string) {
            int length = string.length();
            if (length < annotation.min() || length > annotation.max()) {
                result = false;
                message = getMessage(annotation);
            }
        }

        return new Result(result, message);
    }

    private String getMessage(StringLength annotation) {
        if (!StringUtility.isNullOrBlank(annotation.message())) {
            return annotation.message();
        }
        return String.format("Value must be between %d and %d characters long", annotation.min(), annotation.max());
    }
}
