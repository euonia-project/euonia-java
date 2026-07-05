package com.euonia.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.euonia.bus.inbox.InboxStore;
import com.euonia.bus.message.MessageHandlerContext;
import com.euonia.http.RequestContextAwareExecutor;
import com.euonia.pipeline.DefaultPipelineFactory;
import com.euonia.pipeline.Pipeline;
import com.euonia.pipeline.PipelineFactory;
import com.euonia.reflection.ServiceProvider;
import com.euonia.tuple.Duet;

/**
 * 幂等处理器装饰器，在处理消息前检查 {@link InboxStore} 是否已处理该消息。
 * <p>
 * 用法：
 *
 * <pre>{@code
 * var realHandler = new CreateOrderHandler();
 * var idempotentHandler = new IdempotentHandler<>(realHandler, inboxStore);
 *
 * // 注册到总线
 * handlerContext.register(OrderCmd.class, IdempotentHandler.class);
 * }</pre>
 *
 * @author damon(zhaorong@outlook.com)
 */
final class IdempotentHandler {

    private static final Logger LOGGER = Logger.getLogger(IdempotentHandler.class.getName());
    private final InboxStore inboxStore;
    private final ScheduledExecutorService scheduler;
    private final ServiceProvider provider;
    private final PipelineFactory pipelineFactory;
    private final Map<String, List<MessageHandlerContext>> handlerContainer;
    private final AtomicInteger runningLock = new AtomicInteger(0);

    IdempotentHandler(ServiceProvider provider, Map<String, List<MessageHandlerContext>> handlerContainer) {
        this.inboxStore = provider.getService(InboxStore.class).orElse(null);
        this.provider = provider;
        this.handlerContainer = handlerContainer;
        this.pipelineFactory = provider.getService(PipelineFactory.class)
                                       .orElse(new DefaultPipelineFactory(provider));
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "euonia-inbox-handler");
            t.setDaemon(true);
            return t;
        });
    }

    void start() {
        if (inboxStore == null) {
            return;
        }
        scheduler.scheduleWithFixedDelay(this::retryAll, 0, 60, TimeUnit.SECONDS);
        LOGGER.info(() -> "InboxHandler started with interval 60 SECONDS");
    }

    private void retryAll() {
        if (runningLock.get() > 0) {
            return;
        }
        runningLock.incrementAndGet();
        try {
            var items = inboxStore.getFailedMessages();
            var tasks = new ArrayList<CompletableFuture<Object>>();
            for (var item : items) {
                var entry = inboxStore.getAndCache(item.getMessageId());
                var factories = handlerContainer.getOrDefault(entry.getChannel(), List.of());
                if (factories.isEmpty()) {
                    LOGGER.warning(() -> "No handler found for channel " + entry.getChannel() + ", skipping message " + entry.getMessageId());
                    continue;
                }

                for (var factory : factories) {
                    if (factory.handlerType().getTypeName().equals(item.getHandler())) {
                        LOGGER.info(() -> "Retrying message " + entry.getMessageId() + " with handler " + item.getHandler());
                        var task = executeHandlerAsync(factory, entry.getContent(), null);
                        tasks.add(task);
                        break;
                    }
                }
            }
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new))
                             .whenComplete((v, ex) -> {
                                 if (ex != null) {
                                     LOGGER.log(Level.SEVERE, "Error occurred while retrying messages", ex);
                                 }
                                 inboxStore.clearCache();
                             });
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error occurred while retrying messages", ex);
        } finally {
            runningLock.decrementAndGet();
        }
    }

    /**
     * 在给定的服务范围内异步执行单个处理器工厂。
     * 对于请求/单播消息类型，异常会被重新抛出。
     * 对于多播消息类型，异常会被吞没并作为结果返回。
     *
     * @param factory 处理器工厂
     * @param message 消息对象
     * @param context 消息上下文
     * @return 异步处理结果
     */
    public CompletableFuture<Object> executeHandlerAsync(MessageHandlerContext factory, MessageEnvelope<?> message, MessageContext context) {
        Executor customExecutor = RequestContextAwareExecutor.fromCommonPool();
        Pipeline<Duet<String, MessageEnvelope<?>>, Object> pipeline = pipelineFactory.create();
        if (inboxStore != null) {
            pipeline.use(InboxPipelineBehavior.class, inboxStore);
        }
        return pipeline.runAsync(new Duet<>(factory.handlerType().getTypeName(), message), ctx -> CompletableFuture.supplyAsync(() -> executeHandler(factory, message, context), customExecutor))
                       .toCompletableFuture();
    }

    /**
     * 在给定的服务范围内执行单个处理器工厂。
     * 对于请求/单播消息类型，异常会被重新抛出。
     * 对于多播消息类型，异常会被吞没并作为结果返回。
     *
     * @param factory 处理器工厂
     * @param message 消息对象
     * @param context 消息上下文
     * @return 处理结果
     */
    public Object executeHandler(MessageHandlerContext factory, MessageEnvelope<?> message, MessageContext context) {
        var handler = factory.factory().createHandler(provider);
        return handler.handle(message.getPayload(), context);
    }
}
