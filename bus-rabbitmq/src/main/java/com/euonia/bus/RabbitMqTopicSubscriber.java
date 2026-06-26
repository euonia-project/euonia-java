package com.euonia.bus;

import java.io.IOException;
import java.lang.reflect.Type;

import com.euonia.bus.event.MessageAcknowledgedEvent;
import com.euonia.bus.event.MessageReceivedEvent;
import com.euonia.bus.recipient.Subscriber;
import com.euonia.bus.serialization.MessageSerializer;
import com.euonia.utility.StringUtility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * RabbitMQ 主题订阅者，实现多播消息的消费。
 * <p>
 * 绑定到扇出（fanout）交换器，声明独占队列并消费所有发布到该交换器的消息。
 * 消息处理完成后自动发送 ACK 确认并触发消息确认事件。
 *
 * @author damon(zhaorong@outlook.com)
 */
final class RabbitMqTopicSubscriber extends RabbitMqRecipient implements Subscriber {

    private Channel channel;
    private String recipientTag;

    /**
     * 使用必要的依赖构造主题订阅者。
     *
     * @param connection  RabbitMQ 连接
     * @param options     RabbitMQ 总线选项
     * @param handler     处理器上下文
     * @param serializer  消息序列化器
     * @param messageType 此订阅者处理的消息类型
     */
    public RabbitMqTopicSubscriber(Connection connection, RabbitMqBusOptions options, HandlerContext handler, MessageSerializer serializer, Type messageType) {
        super(connection, options, handler, serializer, messageType);
    }

    /**
     * 启动主题订阅，声明扇出交换器和独占队列，绑定后开始监听消息。
     * <p>
     * 收到消息后：
     * <ol>
     *   <li>反序列化消息体</li>
     *   <li>触发消息接收事件</li>
     *   <li>异步处理消息</li>
     *   <li>处理完成后发送 ACK 确认并触发消息确认事件</li>
     * </ol>
     *
     * @param group 通道组名称，用于生成交换器和队列名
     */
    @Override
    void start(String group) {
        try {
            channel = connection.createChannel();

            var exchangePrefix = StringUtility.collapse(options.getExchangeNamePrefix(), RabbitMqConstants.DEFAULT_EXCHANGE_NAME_PREFIX);
            var exchangeName = String.format("%s:%s", exchangePrefix, group);

            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);
            var dlxArgs = declareDeadLetterInfrastructure(channel, group);
            var queueName = channel.queueDeclare(options.generateQueueName(exchangeName, group), true, false, false, dlxArgs).getQueue();
            channel.queueBind(queueName, exchangeName, "*");
            var consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {

                    RoutedMessage<?> message = serializer.deserialize(new String(body), messageType);
                    var context = new MessageContextBase(message);
                    raiseMessageReceived(new MessageReceivedEvent(message.getPayload(), context));
                    handleAsync(message, context).whenComplete((result, error) -> {
                        try {
                            channel.basicAck(envelope.getDeliveryTag(), false);
                            raiseMessageAcknowledged(new MessageAcknowledgedEvent(message.getPayload(), context));
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
                }
            };

            recipientTag = channel.basicConsume(queueName, false, consumer);

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void stop() {
        if (channel != null && channel.isOpen()) {
            try {
                if (recipientTag != null) {
                    channel.basicCancel(recipientTag);
                }
                channel.close();
            } catch (IOException | java.util.concurrent.TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
