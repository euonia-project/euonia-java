package com.euonia.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * TypeHelper 是一个工具类，提供将值强制转换为目标类型的方法，支持原始类型、枚举、日期/时间转换、集合等。
 * 旨在简化类型转换并确保各种场景下的类型安全。
 *
 * @author damon(zhaorong@outlook)
 */
public final class TypeHelper {
    private TypeHelper() {
    }

    /**
     * 将给定的值强制转换为指定的目标类型。
     *
     * @param desiredType 目标类型
     * @param valueType   值的实际类型（可选，如果为 null 则使用 value 的运行时类型）
     * @param value       要转换的值
     * @return 转换后的值
     * @throws IllegalArgumentException 如果无法转换为目标类型
     */
    public static Object coerceValue(Class<?> desiredType, Class<?> valueType, Object value) {
        if (desiredType == null)
            throw new IllegalArgumentException("desiredType 为 null");

        if (value == null) {
            if (desiredType.isPrimitive()) {
                return defaultPrimitiveValue(desiredType);
            }
            return null;
        }

        if (valueType == null)
            valueType = value.getClass();

        if (desiredType.isAssignableFrom(valueType)) {
            return value;
        }

        Class<?> boxedDesired = boxIfPrimitive(desiredType);
        Class<?> boxedValue = boxIfPrimitive(valueType);

        // 枚举
        if (boxedDesired.isEnum()) {
            return convertToEnum(boxedDesired, value);
        }

        // 日期/时间目标类型
        if (isDateTimeTarget(boxedDesired)) {
            return convertToDateTime(boxedDesired, value);
        }

        // 集合/Map/JSON
        if (Collection.class.isAssignableFrom(boxedDesired) || boxedDesired.isArray()
                || Map.class.isAssignableFrom(boxedDesired)) {
            return convertToCollectionOrMap(boxedDesired, value);
        }

        // String 目标类型
        if (boxedDesired == String.class) {
            return value.toString();
        }

        // Boolean 目标类型
        if (boxedDesired == Boolean.class) {
            return convertToBoolean(value);
        }

        // 数字
        if (Number.class.isAssignableFrom(boxedDesired) || isPrimitiveNumber(desiredType)) {
            return convertToNumber(boxedDesired, value);
        }

        if (boxedDesired == UUID.class) {
            return convertToUUID(value);
        }

        if (boxedDesired == Character.class) {
            return convertToCharacter(value);
        }

        if (boxedDesired.isAssignableFrom(boxedValue)) {
            return value;
        }

        // 最后尝试使用 Jackson 转换（如果可用）
        Object conv = tryJacksonConvert(value, desiredType);
        if (conv != null)
            return conv;

        throw new IllegalArgumentException(String.format("无法将 %s 类型的值转换为 %s（value=%s）",
                value.getClass().getName(), desiredType.getName(), value));
    }

    /**
     * 重载方法：当 valueType 不可用时，直接使用 value 的运行时类型进行转换。
     *
     * @param <T>         目标类型
     * @param desiredType 目标类型
     * @param value       要转换的值
     * @return 转换后的值
     */
    public static <T> T coerceValue(Class<T> desiredType, Object value) {
        return (T) coerceValue(desiredType, (value == null ? null : value.getClass()), value);
    }

    /**
     * 如果给定的类是原始类型，则返回其包装类型；否则返回该类本身。
     *
     * @param type 要检查的类
     * @return 如果是原始类型则返回包装类型，否则返回该类本身
     */
    public static Class<?> boxIfPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    /**
     * 检查给定的类是否是原始数字类型。
     *
     * @param type 要检查的类
     * @return 如果是原始数字类型则返回 true，否则返回 false
     */
    public static boolean isPrimitiveNumber(Class<?> type) {
        return type == int.class || type == long.class || type == short.class || type == byte.class
                || type == float.class || type == double.class;
    }

    /**
     * 获取原始类型的默认值。
     *
     * @param primitiveType 原始类型
     * @return 原始类型的默认值
     */
    public static Object defaultPrimitiveValue(Class<?> primitiveType) {
        if (primitiveType == boolean.class)
            return false;
        if (primitiveType == char.class)
            return '\0';
        if (primitiveType == byte.class)
            return (byte) 0;
        if (primitiveType == short.class)
            return (short) 0;
        if (primitiveType == int.class)
            return 0;
        if (primitiveType == long.class)
            return 0L;
        if (primitiveType == float.class)
            return 0.0f;
        if (primitiveType == double.class)
            return 0.0d;
        return null;
    }

    @SuppressWarnings({ "rawtypes", "IfCanBeSwitch" })
    private static Object convertToEnum(Class<?> enumType, Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Enum) {
            return value;
        }

        if (value instanceof String target) {
            String string = target.trim();
            if (string.isEmpty()) {
                return null;
            }
            try {
                return Enum.valueOf((Class<? extends Enum>) enumType, string);
            } catch (IllegalArgumentException ex) {
                // 尝试按序号匹配
                try {
                    int ord = Integer.parseInt(string);
                    Enum[] constants = (Enum[]) enumType.getEnumConstants();
                    if (ord >= 0 && ord < constants.length)
                        return constants[ord];
                } catch (NumberFormatException ignored) {
                }
                throw ex;
            }
        }

        if (value instanceof Number number) {
            int ord = number.intValue();
            Enum[] constants = (Enum[]) enumType.getEnumConstants();
            if (ord >= 0 && ord < constants.length) {
                return constants[ord];
            }
            throw new IllegalArgumentException("枚举序号超出范围：" + ord);
        }

        String vs = value.toString();
        if (vs != null && !vs.isEmpty()) {
            return convertToEnum(enumType, vs);
        }
        throw new IllegalArgumentException("无法转换为枚举：" + value);
    }

    private static Object convertToBoolean(Object value) {

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean)
            return value;
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String s = value.toString().trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "", "false", "0", "no", "n" -> false;
            case "true", "1", "yes", "y" -> true;
            default -> throw new IllegalArgumentException("无法转换为布尔值：" + value);
        };
    }

    private static Object convertToNumber(Class<?> targetNumberClass, Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return castNumber(number, targetNumberClass);
        }
        String s = value.toString().trim();
        if (s.isEmpty()) {
            return castNumber(BigDecimal.ZERO, targetNumberClass);
        }
        try {
            BigDecimal bd = new BigDecimal(s);
            return castNumber(bd, targetNumberClass);
        } catch (Exception ex) {
            throw new IllegalArgumentException("无法转换为数字：" + value, ex);
        }
    }

    private static Object castNumber(Number number, Class<?> targetNumberClass) {
        if (targetNumberClass == Byte.class)
            return number.byteValue();
        if (targetNumberClass == Short.class)
            return number.shortValue();
        if (targetNumberClass == Integer.class)
            return number.intValue();
        if (targetNumberClass == Long.class)
            return number.longValue();
        if (targetNumberClass == Float.class)
            return number.floatValue();
        if (targetNumberClass == Double.class)
            return number.doubleValue();
        if (targetNumberClass == BigDecimal.class) {
            if (number instanceof BigDecimal)
                return number;
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (targetNumberClass.isInstance(number))
            return number;
        throw new IllegalArgumentException("不支持的目标数字类型：" + targetNumberClass.getName());
    }

    private static Object convertToUUID(Object value) {
        if (value instanceof UUID) {
            return value;
        }
        String s = value.toString().trim();
        if (s.isEmpty())
            return null;
        return UUID.fromString(s);
    }

    private static Object convertToCharacter(Object value) {

        if (value == null) {
            return '\0';
        }
        if (value instanceof Character) {
            return value;
        }
        String s = value.toString();
        if (s.isEmpty())
            return '\0';
        return s.charAt(0);
    }

    /**
     * 扩展转换：日期/时间、集合、Map、JSON（当 Jackson 可用时）
     */
    private static boolean isDateTimeTarget(Class<?> boxedDesired) {
        return boxedDesired == Date.class || boxedDesired == Instant.class || boxedDesired == LocalDateTime.class
                || boxedDesired == LocalDate.class || boxedDesired == LocalTime.class
                || boxedDesired == OffsetDateTime.class
                || boxedDesired == ZonedDateTime.class;
    }

    private static Object convertToDateTime(Class<?> target, Object value) {
        if (value == null)
            return null;

        if (target.isInstance(value))
            return value;

        if (target == Date.class) {
            if (value instanceof Date)
                return value;
            if (value instanceof Number)
                return new Date(((Number) value).longValue());
            String s = value.toString().trim();
            if (s.isEmpty())
                return null;
            try {
                Instant inst = Instant.parse(s);
                return Date.from(inst);
            } catch (DateTimeParseException ignored) {
            }
            try {
                long l = Long.parseLong(s);
                return new Date(l);
            } catch (NumberFormatException ignored) {
            }
            throw new IllegalArgumentException("无法转换为 Date：" + value);
        }

        if (Temporal.class.isAssignableFrom(target) || target == LocalDate.class || target == LocalDateTime.class
                || target == LocalTime.class || target == Instant.class || target == OffsetDateTime.class
                || target == ZonedDateTime.class) {
            if (value instanceof Number) {
                long epoch = ((Number) value).longValue();
                Instant inst = Instant.ofEpochMilli(epoch);
                return convertInstantToTarget(target, inst);
            }

            String s = value.toString().trim();
            if (s.isEmpty())
                return null;
            List<java.util.function.Function<String, Object>> parsers = Arrays.asList(
                    Instant::parse,
                    OffsetDateTime::parse,
                    ZonedDateTime::parse,
                    LocalDateTime::parse,
                    LocalDate::parse,
                    LocalTime::parse);
            for (var p : parsers) {
                try {
                    Object parsed = p.apply(s);

                    return switch (target.getSimpleName()) {
                        case "Instant" -> convertInstantToTarget(target, (Instant) parsed);
                        case "OffsetDateTime" -> convertInstantToTarget(target, ((OffsetDateTime) parsed).toInstant());
                        case "ZonedDateTime" -> convertInstantToTarget(target, ((ZonedDateTime) parsed).toInstant());
                        case "LocalDateTime" -> convertInstantToTarget(target,
                                ((LocalDateTime) parsed).atZone(ZoneId.systemDefault()).toInstant());
                        case "LocalDate" -> convertInstantToTarget(target,
                                ((LocalDate) parsed).atStartOfDay(ZoneId.systemDefault()).toInstant());
                        case "LocalTime" -> convertInstantToTarget(target, LocalDateTime
                                .of(LocalDate.now(), (LocalTime) parsed).atZone(ZoneId.systemDefault()).toInstant());
                        default -> null;
                    };
                } catch (DateTimeParseException ignored) {
                }
            }

            try {
                long l = Long.parseLong(s);
                Instant inst = Instant.ofEpochMilli(l);
                return convertInstantToTarget(target, inst);
            } catch (NumberFormatException ignored) {
            }

            throw new IllegalArgumentException("无法解析日期/时间值：" + value);
        }

        throw new IllegalArgumentException("不支持的日期/时间目标类型：" + target.getName());
    }

    private static Object convertInstantToTarget(Class<?> target, Instant inst) {
        if (target == Instant.class)
            return inst;
        if (target == LocalDateTime.class)
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        if (target == LocalDate.class)
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).toLocalDate();
        if (target == LocalTime.class)
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault()).toLocalTime();
        if (target == OffsetDateTime.class)
            return OffsetDateTime.ofInstant(inst, ZoneId.systemDefault());
        if (target == ZonedDateTime.class)
            return ZonedDateTime.ofInstant(inst, ZoneId.systemDefault());
        if (target == Date.class)
            return Date.from(inst);
        return null;
    }

    private static Object convertToCollectionOrMap(Class<?> desiredType, Object value) {
        if (value == null)
            return null;
        if (desiredType.isInstance(value))
            return value;

        if (value instanceof Map) {
            Object conv = tryJacksonConvert(value, desiredType);
            if (conv != null)
                return conv;
            if (Map.class.isAssignableFrom(desiredType))
                return value;
        }

        if (value instanceof Iterable || value.getClass().isArray()) {
            List<Object> src = new ArrayList<>();
            if (value instanceof Iterable)
                for (Object o : (Iterable<?>) value)
                    src.add(o);
            else {
                int len = Array.getLength(value);
                for (int i = 0; i < len; i++)
                    src.add(Array.get(value, i));
            }

            if (desiredType.isArray()) {
                Class<?> comp = desiredType.getComponentType();
                Object arr = Array.newInstance(comp, src.size());
                for (int i = 0; i < src.size(); i++) {
                    Array.set(arr, i, coerceValue(comp, src.get(i) == null ? null : src.get(i).getClass(), src.get(i)));
                }
                return arr;
            }

            if (Collection.class.isAssignableFrom(desiredType)) {
                try {
                    Collection<Object> coll = (Collection<Object>) desiredType.getDeclaredConstructor().newInstance();
                    coll.addAll(src);
                    return coll;
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                        | NoSuchMethodException | InvocationTargetException ex) {
                    return src;
                }
            }
        }

        if (value instanceof String string) {
            String s = string.trim();
            if (s.startsWith("[") || s.startsWith("{")) {
                Object parsed = tryJacksonParse(string);
                if (parsed != null) {
                    if (desiredType.isInstance(parsed))
                        return parsed;
                    Object conv = tryJacksonConvert(parsed, desiredType);
                    if (conv != null)
                        return conv;
                }
            }
        }

        if (desiredType == Map.class && value instanceof String) {
            Object parsed = tryJacksonParse((String) value);
            if (parsed instanceof Map)
                return parsed;
        }

        throw new IllegalArgumentException("无法转换为集合/Map 目标类型：" + desiredType.getName());
    }

    /**
     * 基于反射的可选 Jackson 集成：使用 ObjectMapper 将 JSON 字符串解析为对象
     */
    private static Object tryJacksonParse(String json) {
        try {
            Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object mapper = mapperClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method read = mapperClass.getMethod("readValue", String.class, Class.class);
            return read.invoke(mapper, json, Object.class);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException
                | NoSuchMethodException | InvocationTargetException ex) {
            return null;
        }
    }

    private static Object tryJacksonConvert(Object value, Class<?> desiredType) {
        try {
            Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object mapper = mapperClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method convert = mapperClass.getMethod("convertValue", Object.class, Class.class);
            return convert.invoke(mapper, value, desiredType);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException
                | NoSuchMethodException | InvocationTargetException ex) {
            return null;
        }
    }
}
