package com.euonia.annotation;

import com.euonia.utility.StringUtility;

/**
 * RangeValidator 是一个用于验证数值是否在指定范围内的验证器类。
 * 它实现了 Validator 接口，并提供了 validate 方法来执行验证逻辑。
 * <p>
 * 验证逻辑如下：
 * <ul>
 *     <li>如果值是一个数字，则检查其是否在注解指定的最小值和最大值之间，并根据 inclusiveMin 和 inclusiveMax 属性判断是否包含边界值。</li>
 *     <li>如果数值不在指定范围内，则验证失败，并返回指定的错误消息或默认消息 "Value must be between {min} and {max}{inclusiveMin}{inclusiveMax}"。</li>
 *     <li>如果值不是数字，则验证成功。</li>
 * </ul>
 * <p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class RangeValidator implements Validator<Range> {
    @Override
    public Result validate(Range annotation, Object value) {
        boolean result = true;
        String message = null;

        if (value instanceof Number number) {
            if (number.doubleValue() > annotation.max()) {
                result = false;
                message = getMessage(annotation);
            } else if (number.doubleValue() < annotation.min()) {
                result = false;
                message = getMessage(annotation);
            } else if (number.doubleValue() == annotation.min() && !annotation.inclusiveMin()) {
                result = false;
                message = getMessage(annotation);
            } else if (number.doubleValue() == annotation.max() && !annotation.inclusiveMax()) {
                result = false;
                message = getMessage(annotation);
            }
        }

        return new Result(result, message);
    }

    private String getMessage(Range annotation) {

        if (!StringUtility.isNullOrBlank(annotation.message())) {
            return annotation.message();
        }

        return String.format("Value must be between %s and %s%s%s",
                             annotation.min(),
                             annotation.max(),
                             annotation.inclusiveMin() ? " (inclusive)" : "",
                             annotation.inclusiveMax() ? " (inclusive)" : ""
        );
    }
}
