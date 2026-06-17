package com.euonia.bus;

import com.euonia.bus.exception.MessageTypeException;
import com.euonia.bus.options.MessageBusOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于已配置的策略和约定为消息类型确定传输的分发器。
 * 它使用缓存来存储消息类型的传输判定结果以提高性能。
 *
 * @author damon(zhaorong@outlook)
 */
public class StrategicDispatcher implements Dispatcher {
    private final ConcurrentHashMap<Class<?>, List<String>> transportCache = new ConcurrentHashMap<>();
    private final MessageBusOptions options;

    /**
     * 使用指定的选项创建 {@link StrategicDispatcher} 的新实例。
     *
     * @param options 消息总线选项
     */
    public StrategicDispatcher(MessageBusOptions options) {
        this.options = options;
    }

    /**
     * 为指定的消息类型创建传输列表。
     *
     * @param messageType 消息的类型
     * @return 消息应被分发到的传输名称列表
     */
    @Override
    public List<String> determine(Class<?> messageType) {
        var transportTypes = transportCache.computeIfAbsent(messageType, t -> {
            var list = new ArrayList<String>();
            for (var type : options.getStrategyAssignedTypes()) {
                var strategy = options.getStrategy(type);
                if (strategy.outgoing(messageType)) {
                    list.add(type);
                }
            }
            return list;
        });

        switch (transportTypes.size()) {
            case 0:
                if (options.getDefaultTransport() == null || options.getDefaultTransport().isEmpty()) {
                    throw new MessageTypeException("No transport is configured for the message type. Message type: " + messageType.getName());
                }
                transportTypes.add(options.getDefaultTransport());
                break;
            case 1:
                break;
            default:
                if (!options.getConvention().isMulticastType(messageType)) {
                    throw new MessageTypeException("The message type is not identified as a multicast type, but multiple transport strategies are configured for it. Message type: " + messageType.getName());
                }
                break;
        }

        return transportTypes;
    }
}
