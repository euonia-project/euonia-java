package com.euonia.annotation;

import com.euonia.tuple.Duet;
import com.euonia.utility.StringUtility;

/**
 * RequiredValidator 是一个用于验证对象是否满足 Required 注解约束的验证器类。
 * 它实现了 Validator 接口，并提供了 validate 方法来执行验证逻辑。
 * <p>
 * 验证逻辑如下：
 * <ul>
 *     <li>如果值为 null，则验证失败，并返回指定的错误消息或默认消息 "Value is required"。</li>
 *     <li>如果值是一个空字符串且 allowEmpty 为 false，则验证失败，并返回指定的错误消息或默认消息 "Value must not be empty"。</li>
 *     <li>如果值不为 null 且不为空字符串（当 allowEmpty 为 false 时），则验证成功。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RequiredValidator implements Validator<Required> {
    @Override
    public Duet<Boolean, String> validate(Required annotation, Object value) {
        Duet<Boolean, String> result;
        if (value == null) {
            result = new Duet<>(false, StringUtility.collapse(annotation.message(), "Value is required"));
        } else if (!annotation.allowEmpty() && value instanceof String string && string.isEmpty()) {
            result = new Duet<>(false, StringUtility.collapse(annotation.message(), "Value must not be empty"));
        } else {
            result = new Duet<>(true, null);
        }
        return result;
    }
}
