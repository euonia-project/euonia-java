package com.euonia.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.euonia.bus.exception.MessageTypeException;

/**
 * 基于已配置的策略和约定为消息类型确定传输的分发器。
 * 它使用缓存来存储消息类型的传输判定结果以提高性能。
 *
 * @author damon(zhaorong@outlook.com)
 */
class StrategicDispatcher implements Dispatcher {
    private final ConcurrentHashMap<String, List<String>> transportCache = new ConcurrentHashMap<>();
    private final Configurator configurator;

    /**
     * 使用指定的配置器创建 {@link StrategicDispatcher} 的新实例。
     *
     * @param configurator 消息总线配置器
     */
    StrategicDispatcher(Configurator configurator) {
        this.configurator = configurator;
    }

    /**
     * 为指定的消息类型创建传输列表。
     *
     * @param messageType 消息的类型
     * @return 消息应被分发到的传输名称列表
     */
    @Override
    public List<String> determine(String channel) {
        var transportTypes = transportCache.computeIfAbsent(channel, t -> {
            var list = new ArrayList<String>();
            for (var type : configurator.getStrategyAssignedTypes()) {
                var strategy = configurator.getStrategy(type);
                if (strategy != null && strategy.allowOutgoing(channel)) {
                    list.add(type);
                }
            }
            return list;
        });

        switch (transportTypes.size()) {
            case 0 -> {
                if (configurator.getDefaultTransport() == null || configurator.getDefaultTransport().isEmpty()) {
                    throw new MessageTypeException("No transport is configured for the message type. Channel: " + channel);
                }
                transportTypes.add(configurator.getDefaultTransport());
            }
            case 1 -> {
            }
            default -> {
                if (!configurator.getConvention().isMulticast(channel)) {
                    throw new MessageTypeException("The message type is not identified as a multicast type, but multiple transport strategies are configured for it. Channel: " + channel);
                }
            }
        }

        return transportTypes;
    }
}
