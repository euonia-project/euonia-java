package com.euonia.bus;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.euonia.bus.annotation.Subscribe;
import com.euonia.bus.message.MessageCache;
import com.euonia.reflection.ClassScanner;

/**
 * 消息处理器查找器，负责扫描指定包或处理器类型并生成 {@link HandlerRegistration} 列表。
 * <p>
 * 支持两种查找方式：
 * <ul>
 *   <li>按包扫描 —— 扫描指定包下所有类中标注了 {@link Subscribe} 注解的方法</li>
 *   <li>按类型解析 —— 解析指定的处理器类型，同时支持 {@link Subscribe} 注解方法和 {@link Handler} 接口实现</li>
 * </ul>
 *
 * @author damon(zhaorong@outlook.com)
 */
public class MessageHandlerFinder {

    /**
     * 扫描指定包下的所有类，查找其中带有 {@link Subscribe} 注解的方法，
     * 并为每个匹配的方法生成一个 {@link HandlerRegistration}。
     *
     * @param packages 要扫描的包名数组
     * @return 找到的处理器注册信息列表
     */
    public static List<HandlerRegistration> find(String... packages) {
        List<HandlerRegistration> registrations = new ArrayList<>();
        for (var pkg : packages) {
            var classes = ClassScanner.scan(pkg);
            for (var cls : classes) {
                registrations.addAll(resolve(cls));
            }
        }
        return registrations;
    }

    /**
     * 解析指定的处理器类型，同时查找 {@link Subscribe} 注解方法和 {@link Handler} 接口实现，
     * 并为每个匹配的方法生成一个 {@link HandlerRegistration}。
     *
     * @param handlerTypes 要解析的处理器类型数组
     * @return 找到的处理器注册信息列表
     */
    public static List<HandlerRegistration> find(Class<?>... handlerTypes) {
        List<HandlerRegistration> registrations = new ArrayList<>();
        for (var handlerType : handlerTypes) {
            registrations.addAll(resolve(handlerType));
        }
        return registrations;
    }

    /**
     * 解析单个处理器类型，查找其中的消息处理方法。
     * <p>
     * 处理流程分为两步：
     * <ol>
     *   <li>查找带有 {@link Subscribe} 注解的方法，提取通道名和消息类型</li>
     *   <li>查找 {@link Handler} 接口实现，通过泛型参数推断消息类型</li>
     * </ol>
     * <p>
     * 接口、记录、枚举和数组类型将被跳过。
     *
     * @param handlerType 要解析的处理器类
     * @return 该处理器类型对应的注册信息列表
     * @throws IllegalArgumentException 如果 {@link Subscribe} 方法签名不符合规范
     */
    private static List<HandlerRegistration> resolve(Class<?> handlerType) {
        if (handlerType.isInterface() || handlerType.isRecord() || handlerType.isEnum() || handlerType.isArray()) {
            return List.of();
        }
        List<HandlerRegistration> registrations = new ArrayList<>();

        var methods = Arrays.stream(handlerType.getDeclaredMethods())
                            .filter(m -> m.getAnnotation(Subscribe.class) != null)
                            .toList();

        if (!methods.isEmpty()) {
            for (var method : methods) {
                var parameters = method.getParameters();
                if (parameters.length < 1) {
                    throw new IllegalArgumentException("Method " + method.getName() + " in class " + handlerType.getName() + " must have at least one parameter.");
                }
                var firstParam = parameters[0];
                if (firstParam.getType().isPrimitive()) {
                    throw new IllegalArgumentException("First parameter of method " + method.getName() + " in class " + handlerType.getName() + " cannot be a primitive type.");
                }
                if (parameters.length == 2) {
                    if (!MessageContext.class.isAssignableFrom(parameters[1].getType())) {
                        throw new IllegalArgumentException("Second parameter of method " + method.getName() + " in class " + handlerType.getName() + " must be of type MessageContext or its subtype.");
                    }
                }
                var annotation = method.getAnnotation(Subscribe.class);
                var channel = annotation.value();
                if (channel == null || channel.isBlank()) {
                    channel = MessageCache.getInstance().getOrAddChannel(firstParam.getType());
                }

                var registration = new HandlerRegistration(channel, firstParam.getType(), handlerType, method);
                registrations.add(registration);
            }
        }

        var interfaces = Arrays.stream(handlerType.getGenericInterfaces())
                               .filter(i -> i instanceof ParameterizedType pt && pt.getRawType() == Handler.class)
                               .toList();

        if (!interfaces.isEmpty()) {

            for (var genericInterface : interfaces) {
                var parameterizedType = (ParameterizedType) genericInterface;
                var messageType = parameterizedType.getActualTypeArguments()[0];
                try {
                    var method = handlerType.getMethod("handle", (Class<?>) messageType, MessageContext.class);
                    var channel = MessageCache.getInstance().getOrAddChannel((Class<?>) messageType);
                    var registration = new HandlerRegistration(channel, (Class<?>) messageType, handlerType, method);
                    registrations.add(registration);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return registrations;
    }
}
