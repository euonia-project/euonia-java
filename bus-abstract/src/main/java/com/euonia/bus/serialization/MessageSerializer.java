package com.euonia.bus.serialization;

import com.euonia.bus.MessageEnvelope;

import java.lang.reflect.Type;

/**
 * 消息序列化器接口，定义了消息的序列化和反序列化方法。
 * 实现该接口的类可以使用不同的序列化方式（如JSON、XML、Protobuf等）来处理消息的序列化和反序列化。
 *
 * @author damon(zhaorong@outlook.com)
 * @apiNote 该接口提供了两种序列化方式：字符串和字节数组。实现类可以根据需要选择适合的序列化方式。
 */
public interface MessageSerializer {
    /**
     * 将消息对象序列化为字符串。
     *
     * @param <M>     消息对象的类型
     * @param message 消息对象
     * @return 序列化后的字符串
     */
    <M> String serialize(M message);

    /**
     * 将字符串反序列化为消息对象。
     *
     * @param <M>  消息对象的类型
     * @param data 序列化后的字符串
     * @param type 消息对象的类型
     * @return 反序列化后的消息对象
     */
    <M> M deserialize(String data, Class<M> type);

    /**
     * 将字符串反序列化为消息对象（支持泛型类型）。
     * 当需要反序列化的目标类型包含泛型参数时（如 {@code RoutedMessage<ConcretePayload>}），
     * 应优先使用此方法以确保类型信息不会丢失。
     *
     * @param <M>  消息对象的类型
     * @param data 序列化后的字符串
     * @param type 消息对象的类型（支持参数化类型）
     * @return 反序列化后的消息对象
     */
    <M> M deserialize(String data, Type type);

    /**
     * 将字符串反序列化为消息对象（类型未知）。
     *
     * @param data 序列化后的字符串
     * @return 反序列化后的消息对象
     */
    Object deserialize(String data);

    /**
     * 将消息对象序列化为字节数组。
     *
     * @param <M>     消息对象的类型
     * @param message 消息对象
     * @return 序列化后的字节数组
     */
    <M> byte[] serializeToBytes(M message);

    /**
     * 将字节数组反序列化为消息对象。
     *
     * @param <M>  消息对象的类型
     * @param data 序列化后的字节数组
     * @param type 消息对象的类型
     * @return 反序列化后的消息对象
     */
    <M> M deserializeFromBytes(byte[] data, Class<M> type);

    /**
     * 将字节数组反序列化为消息对象（类型未知）。
     *
     * @param data 序列化后的字节数组
     * @return 反序列化后的消息对象
     */
    Object deserializeFromBytes(byte[] data);

    /**
     * 将消息信封序列化为字符串。
     *
     * @param <V>      消息负载的类型
     * @param envelope 消息信封
     * @return 序列化后的字符串
     */
    <V> String serializeEnvelope(MessageEnvelope<V> envelope);

    /**
     * 将消息信封反序列化为消息对象。
     *
     * @param <V>         消息负载的类型
     * @param data        序列化后的字符串
     * @param payloadType 消息负载的类型
     * @return 反序列化后的消息信封
     */
    <V> MessageEnvelope<V> deserializeEnvelope(String data, Class<V> payloadType);
}
