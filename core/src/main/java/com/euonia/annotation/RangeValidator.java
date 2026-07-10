package com.euonia.annotation;

import com.euonia.utility.StringUtility;

import java.util.regex.Pattern;

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
    private static final String EXPRESSION = "^(?<left>[\\[(])\\s*(?<min>-?(?:\\d+(?:\\.\\d+)?|\\.\\d+))\\s*,\\s*(?<max>-?(?:\\d+(?:\\.\\d+)?|\\.\\d+))\\s*(?<right>[\\])])$";

    private final Pattern pattern = Pattern.compile(EXPRESSION);

    @Override
    public Result validate(Range annotation, Object value) {
        boolean result = true;
        String message = null;

        double min;
        double max;
        Range.Boundary minBoundary;
        Range.Boundary maxBoundary;

        if (!StringUtility.isNullOrEmpty(annotation.value())) {

            var matcher = pattern.matcher(annotation.value());

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid range format in annotation value: " + annotation.value());
            }

            min = Double.parseDouble(matcher.group("min").trim());
            max = Double.parseDouble(matcher.group("max").trim());
            minBoundary = switch (matcher.group("left")) {
                case "[" -> Range.Boundary.INCLUSIVE;
                case "(" -> Range.Boundary.EXCLUSIVE;
                default ->
                    throw new IllegalArgumentException("Invalid left boundary in range: " + matcher.group("left"));
            };
            maxBoundary = switch (matcher.group("right")) {
                case "]" -> Range.Boundary.INCLUSIVE;
                case ")" -> Range.Boundary.EXCLUSIVE;
                default ->
                    throw new IllegalArgumentException("Invalid right boundary in range: " + matcher.group("right"));
            };
        } else {
            min = annotation.min();
            max = annotation.max();
            minBoundary = annotation.minBoundary();
            maxBoundary = annotation.maxBoundary();
        }

        if (value instanceof Number number) {
            if (number.doubleValue() > max) {
                result = false;
                message = getMessage(annotation);
            } else if (number.doubleValue() < min) {
                result = false;
                message = getMessage(annotation);
            } else if (number.doubleValue() == min && minBoundary == Range.Boundary.EXCLUSIVE) {
                result = false;
                message = getMessage(annotation);
            } else if (number.doubleValue() == max && maxBoundary == Range.Boundary.EXCLUSIVE) {
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
                             annotation.minBoundary() == Range.Boundary.INCLUSIVE ? " (inclusive)" : "",
                             annotation.maxBoundary() == Range.Boundary.INCLUSIVE ? " (inclusive)" : ""
        );
    }
}
