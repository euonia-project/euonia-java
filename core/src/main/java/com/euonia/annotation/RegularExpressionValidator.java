package com.euonia.annotation;

import com.euonia.tuple.Duet;
import com.euonia.utility.StringUtility;

/**
 * RegularExpressionValidator 是一个用于验证对象是否满足 RegularExpression 注解约束的验证器类。
 * 它实现了 Validator 接口，并提供了 validate 方法来执行验证逻辑。
 * <p>
 * 验证逻辑如下：
 * <ul>
 *     <li>如果值是一个字符串，则检查该字符串是否匹配指定的正则表达式模式。</li>
 *     <li>如果字符串不匹配正则表达式，则验证失败，并返回指定的错误消息或默认消息 "Value must match the regular expression: {pattern}"。</li>
 *     <li>如果值不是字符串，则验证成功。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RegularExpressionValidator implements Validator<RegularExpression> {
    @Override
    public Duet<Boolean, String> validate(RegularExpression annotation, Object value) {
        boolean result = true;
        String message = null;

        if (value instanceof String string) {
            if (!string.matches(annotation.value())) {
                result = false;
                message = getMessage(annotation);
            }
        }

        return new Duet<>(result, message);
    }

    private String getMessage(RegularExpression annotation) {
        if (!StringUtility.isNullOrBlank(annotation.message())) {
            return annotation.message();
        }
        return String.format("Value must match the regular expression: %s", annotation.value());
    }
}
