package com.euonia.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象泛型工具类，用于在运行时解析泛型参数的实际类型。
 * <p>
 * 通过匿名子类捕获泛型类型参数（"钻石"语法），然后扫描类层次结构中的
 * 类型变量到实际类型的映射关系，最终解析出具体的泛型类型。
 * <p>
 * 使用示例：
 *
 * <pre>{@code
 * Generic<MyService> generic = Generic.forType(MyService.class);
 * Class<? super MyService> resolved = generic.resolve();
 * }</pre>
 *
 * @param <C> 要解析的泛型类型
 * @author damon(zhaorong@outlook.com)
 */
public abstract class Generic<C> {

    /**
     * 已解析泛型的缓存，以 Generic 实例为键
     */
    private static Map<Generic<?>, Type> RESOLVED_GENERICS = new ConcurrentHashMap<>();

    /**
     * 上下文类
     */
    private final Class<?> context;
    /**
     * 捕获的泛型类型参数（"钻石"）
     */
    private final Type diamond;

    /**
     * 使用指定的上下文类构造 Generic 实例，并捕获泛型参数。
     *
     * @param context 上下文类
     */
    protected Generic(Class<?> context) {
        this.context = context;
        this.diamond = capture();
    }

    /**
     * 为指定类型创建静态的 {@link Generic} 实例。
     *
     * @param <T>  要解析的类型
     * @param type 类型的 {@link Class}
     * @return 新的 Generic 实例
     */
    public static <T> Generic<T> forType(Class<T> type) {
        return new Generic<>(type) {
        };
    }

    /**
     * 从匿名子类中捕获泛型类型参数。
     *
     * @return 捕获的泛型类型
     * @throws IllegalArgumentException 如果父类不是参数化类型
     */
    private Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType))
            throw new IllegalArgumentException(superclass + " isn't parameterized");

        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * 解析泛型参数的实际类型，结果会被缓存。
     *
     * @return 解析后的具体类
     */
    @SuppressWarnings("unchecked")
    public Class<? super C> resolve() {
        return (Class<? super C>) RESOLVED_GENERICS.computeIfAbsent(
            this,
            it -> {
                Mappings mappings = new Scanner().scan(context);
                return mappings.get(diamond);
            });
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof Generic<?> other)) {
            return false;
        }
        return context.equals(other.context) && diamond.equals(other.diamond);
    }

    @Override
    public int hashCode() {
        return 31 * context.hashCode() + diamond.hashCode();
    }

    /**
     * 设置泛型解析缓存，用于测试或自定义缓存策略。
     *
     * @param cache 新的缓存映射
     */
    static void setCache(ConcurrentHashMap<Generic<?>, Type> cache) {
        Generic.RESOLVED_GENERICS = cache;
    }

    /**
     * 遍历类层次结构，收集类型变量与实际类型之间的映射关系。
     */
    private static class Scanner {

        private final Mappings mappings = new Mappings();

        /**
         * 扫描指定类的父类和接口，收集泛型映射。
         *
         * @param clazz 要扫描的类
         * @return 收集到的泛型映射
         */
        public Mappings scan(Class<?> clazz) {
            scanSuperclass(clazz);
            scanInterfaces(clazz);
            return mappings;
        }

        private void scanSuperclass(Class<?> clazz) {
            Type superclass = clazz.getGenericSuperclass();
            if (superclass instanceof ParameterizedType parameterizedType) {
                mappings.add(parameterizedType);
                scan((Class<?>) parameterizedType.getRawType());
            } else if (superclass instanceof Class) {
                scan((Class<?>) superclass);
            }
        }

        private void scanInterfaces(Class<?> clazz) {
            for (Type interfaceType : clazz.getGenericInterfaces()) {
                if (interfaceType instanceof ParameterizedType parameterizedType) {
                    mappings.add(parameterizedType);
                    scan((Class<?>) parameterizedType.getRawType());
                }
            }
        }
    }

    /**
     * 泛型映射表，维护类型变量到实际类型的映射。
     */
    private static class Mappings {

        // "类型变量 → 实际类型" 的映射。
        // 例如：如果 MyHandler 实现 Handler<UserCommand, Result>，
        // 且 Handler 定义为 Handler<C extends Command<R>, R>
        // 则映射为：C → UserCommand, R → Result。
        private final Map<TypeVariable<?>, Type> mappings = new HashMap<>();

        /**
         * 从参数化类型中添加类型变量到实际参数的映射。
         *
         * @param type 参数化类型
         */
        public void add(ParameterizedType type) {
            TypeVariable<?>[] generics = ((Class<?>) type.getRawType()).getTypeParameters();
            Type[] concretes = type.getActualTypeArguments();
            for (int i = 0; i < generics.length; i++) {
                mappings.put(generics[i], concretes[i]);
            }
        }

        /**
         * 获取泛型参数的实际类型，递归解析直到得到具体类。
         *
         * @param type 要解析的类型
         * @return 解析后的实际类型
         */
        public Type get(Type type) {
            if (type instanceof TypeVariable) {
                // 如果是类型变量（如 "C" 或 "R"），在映射中查找其绑定的实际类型。
                Type replacement = mappings.get(type);
                // 递归解析，以防替换结果本身是另一个类型变量。
                if (replacement != null)
                    return get(replacement);
            } else if (type instanceof ParameterizedType parameterizedType) {
                // 例如：List<String> → List.class
                return parameterizedType.getRawType();
            }

            // 如果已经是原始类（如 String.class），直接返回。
            return type;
        }
    }
}
