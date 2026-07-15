package com.euonia.bus;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.euonia.bus.annotation.Channel;
import com.euonia.bus.annotation.Subscribe;
import com.euonia.bus.message.Message;
import com.euonia.core.PriorityValueFinder;
import com.euonia.reflection.ClassScanner;
import com.euonia.utility.Resource;
import com.euonia.utility.StringUtility;

/**
 * 消息处理器查找器，负责扫描指定包或处理器类型并生成 {@link ChannelRegistration} 列表。
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

    private static final String RESOURCE_NAME = "resource";

    /**
     * 扫描指定包下的所有类，查找其中带有 {@link Subscribe} 注解的方法，
     * 并为每个匹配的方法生成一个 {@link ChannelRegistration}。
     *
     * @param packages 要扫描的包名数组
     */
    public static void find(Delegate delegate, String... packages) {
        for (var pkg : packages) {
            var classes = ClassScanner.scan(pkg);
            for (var cls : classes) {
                resolve(delegate, cls);
            }
        }
    }

    /**
     * 解析指定的处理器类型，同时查找 {@link Subscribe} 注解方法和 {@link Handler} 接口实现，
     * 并为每个匹配的方法生成一个 {@link ChannelRegistration}。
     *
     * @param handlerTypes 要解析的处理器类型数组
     */
    public static void find(Delegate delegate, Class<?>... handlerTypes) {
        for (var handlerType : handlerTypes) {
            resolve(delegate, handlerType);
        }
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
     * @throws IllegalArgumentException 如果 {@link Subscribe} 方法签名不符合规范
     */
    private static void resolve(Delegate delegate, Class<?> handlerType) {
        if (handlerType.isInterface() || handlerType.isRecord() || handlerType.isEnum() || handlerType.isArray()) {
            return;
        }

        var declaredMethods = new java.util.ArrayList<>(List.of(handlerType.getDeclaredMethods()));

        var interfaces = Arrays.stream(handlerType.getGenericInterfaces())
                               .filter(i -> i instanceof ParameterizedType pt && pt.getRawType() == Handler.class)
                               .toList();

        if (!interfaces.isEmpty()) {
            for (var genericInterface : interfaces) {
                var parameterizedType = (ParameterizedType) genericInterface;
                var messageType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                try {
                    var method = handlerType.getMethod("handle", messageType, MessageContext.class);
                    var annotation = method.getAnnotation(Subscribe.class);
                    var channel = annotation == null ? messageType.getName() : annotation.value();
                    delegate.next(channel, messageType, new ChannelHandler(handlerType, method, null));
                    declaredMethods.remove(method); // 移除已处理的方法，避免重复注册
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!declaredMethods.isEmpty()) {
            for (var method : declaredMethods) {

                // 检查方法是否带有 @Subscribe 注解，带有注解的方法将被注册为消息处理器，未带注解的方法将被忽略
                // 如果方法带有 @Subscribe 注解，则必须至少有一个参数，且第一个参数为消息类型，第二个参数（如果存在）必须为 MessageContext 类型
                // 方法对应的通道名将按以下优先级查找：
                // 1. 优先从方法的 @Subscribe 注解中获取通道名
                // 2. 其次从参数的 @Channel 注解中获取通道名
                // 3. 最后从参数类型的 @Channel 注解中获取通道名
                // 4. 如果以上都未指定通道名，则使用参数类型的全限定类名作为通道名，此时参数类型必须实现 Message 接口，否则抛出异常

                var subscribeAnnotation = method.getAnnotation(Subscribe.class);

                if (Objects.isNull(subscribeAnnotation)) {
                    continue;
                }

                var parameters = method.getParameters();
                if (parameters.length < 1) {
                    throw new IllegalStateException(Resource.getString(RESOURCE_NAME, "MessageHandlerFinder.MethodMustContainOneParameter", handlerType.getName(), method.getName()));
                }
                var messageParameter = parameters[0];
                if (parameters.length == 2 && parameters[1].getType() != MessageContext.class) {
                    throw new IllegalStateException(Resource.getString(RESOURCE_NAME, "MessageHandlerFinder.SecondParameterMustBeOfTypeMessageContext", handlerType.getName(), method.getName()));
                }

                String channel = PriorityValueFinder.find(queue -> {
                    // 1. 优先从方法的 @Subscribe 注解中获取通道名
                    queue.add(subscribeAnnotation::value, 1);
                    // 2. 其次从参数的 @Channel 注解中获取通道名
                    queue.add(() -> {
                        var annotation = messageParameter.getAnnotation(Channel.class);
                        return annotation == null ? null : annotation.value();
                    }, 2);
                    // 3. 最后从参数类型的 @Channel 注解中获取通道名
                    queue.add(() -> {
                        var annotation = messageParameter.getType().getAnnotation(Channel.class);
                        return annotation == null ? null : annotation.value();
                    }, 3);
                }, value -> !StringUtility.isNullOrBlank(value), null);

                if (StringUtility.isNullOrBlank(channel)) {
                    if (!Message.class.isAssignableFrom(messageParameter.getType())) {
                        throw new IllegalStateException(Resource.getString(RESOURCE_NAME, "MessageHandlerFinder.ParameterTypeMustImplementMessageInterface", messageParameter.getType().getName(), handlerType.getName(), method.getName()));
                    }
                    channel = messageParameter.getType().getName();
                }

                delegate.next(channel, messageParameter.getType(), new ChannelHandler(handlerType, method, null));
            }
        }
    }

    /**
     * 代理接口，用于处理找到的消息处理器。
     */
    @FunctionalInterface
    public interface Delegate {
        /**
         * 处理找到的消息处理器。
         *
         * @param channel     通道名称
         * @param messageType 消息类型
         * @param handler     消息处理器封装对象
         */
        void next(String channel, Class<?> messageType, ChannelHandler handler);
    }
}
