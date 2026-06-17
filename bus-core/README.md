# Bus Core 模块

Euonia 消息总线的编排引擎层。在 `bus-abstract` 定义的抽象契约之上，提供消息总线运行时实现（`MessageBus`），包括分发决策、处理器注册与调用、消息约定与策略配置、管道行为集成和自动处理器发现。

---

## 架构

```
                         ┌──────────────────────────────┐
                         │         MessageBus            │
                         │   (publish / send / call)     │
                         └──────────────┬───────────────┘
                                        │
        ┌───────────────────────────────┼───────────────────────────────┐
        │                               │                               │
        ▼                               ▼                               ▼
┌───────────────┐            ┌─────────────────────┐        ┌─────────────────────┐
│   Dispatcher  │            │   MessageBusOptions  │        │   PipelineFactory    │
│ (传输名决策)   │            │  (约定 / 策略 / 默认通道)│        │  (中间件行为)         │
└───────┬───────┘            └──────────┬──────────┘        └─────────────────────┘
        │                               │
        │                    ┌──────────┴──────────┐
        │                    │                     │
        ▼                    ▼                     ▼
┌───────────────┐    ┌──────────────┐    ┌──────────────────┐
│   Transport   │    │ MessageConv. │    │ TransportStrategy │
│  (抽象传输)    │    │ (消息分类)    │    │   (传输路由)       │
└───────────────┘    └──────────────┘    └──────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    DefaultConfigurator                        │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────┐ │
│  │ Conv. Builder    │  │ Strategy Builders │  │ Registrations│ │
│  └─────────────────┘  └──────────────────┘  └─────────────┘ │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │ MessageHandler   │
                    │     Finder       │  ← 包扫描 / @Subscribe / Handler<R>
                    └──────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │ DefaultHandler   │
                    │    Context       │  ← 处理器注册 + 调用运行时
                    └──────────────────┘
```

---

## 核心概念

### MessageBus — 消息总线编排引擎

`MessageBus` 是整个总线的核心类，实现 `Bus` 接口，提供三种消息投递模式：

| 模式 | 方法 | 消息类型 | 传输数 | 说明 |
|------|------|----------|--------|------|
| **发布** | `publishAsync(message, ...)` | `Multicast` | 多个 | 多播到所有匹配的传输；返回 `CompletableFuture<Void>` |
| **发送** | `sendAsync(message, ...)` | `Unicast` | 单个 | 单播到匹配的传输；可选 `Flow.Subscriber<R>` 回调 |
| **调用** | `callAsync(request, ...)` | `Request<R>` | 单个 | 请求-响应；返回 `CompletableFuture<R>` |

每次投递的执行流程：

1. **类型校验** — 使用 `MessageConvention` 验证消息类型与投递模式匹配
2. **上下文解析** — 从 `RequestContextAccessor` 获取当前请求的 trace/auth 信息
3. **通道解析** — 从选项的 `channel` 或 `MessageCache` 解析目标通道
4. **构建信封** — 创建 `RoutedMessage<T>`，填充 messageId、correlationId、requestTrackId、authorization
5. **管道行为** — 可选：通过 `PipelineFactory` 创建管道，附加默认行为后执行
6. **分发决策** — 调用 `Dispatcher.determine()` 获取目标传输名列表
7. **传输投递** — 通过 `ServiceProvider` 解析 `Transport` 实例并调用对应方法

### Handler — 类型化处理器

```java
@FunctionalInterface
public interface Handler<M, R> {
    R handle(M message, MessageContext context);
}
```

应用层可实现的类型化处理器。`DefaultHandlerContext` 通过 `MessageHandlerFactory` 自动适配为内部执行器。

### StrategicDispatcher — 策略分发器

实现 `Dispatcher` 接口，将消息类型映射为传输名称列表。

**决策逻辑：**
1. 查找缓存 → 缓存未命中时遍历 `MessageBusOptions` 中所有已注册的策略
2. 对每个策略调用 `outgoing(messageType)`，收集返回 `true` 的策略名
3. **基数校验：**
   - 0 个匹配 → 使用默认传输（`defaultTransport`），或抛出 `MessageTypeException`
   - 1 个匹配 → 直接使用
   - 多个匹配 → 仅当消息类型为 `Multicast` 时允许，否则抛出异常
4. 结果缓存至 `ConcurrentHashMap`

### DefaultHandlerContext — 处理器运行时

管理处理器的注册、订阅事件和异步调用。

| 功能 | 说明 |
|------|------|
| `register(messageType, handlerType)` | 注册类型化处理器，推导通道 |
| `register(HandlerRegistration)` | 注册反射元数据处理器 |
| `handleAsync(message, context)` | 单通道调用 |
| `handleAsync(channel, message, context)` | 指定通道调用 |
| `onMessageSubscribed(listener)` | 订阅注册事件 |

**调用策略：**
- **单处理器** → 执行后将结果写入 `context.response()`，异常写入 `context.failure()`
- **多处理器** → 并行执行所有处理器（多播模式，忽略返回值）
- **异常处理** → `Multicast` 消息吞掉异常继续扇出；`Unicast`/`Request` 抛出异常

**参数解析：** 支持反射方法的多参数注入（0-3 个参数），自动将 `MessageContext` 按类型注入对应位置。

### DefaultConfigurator — 配置根

流式配置中枢，聚合三要素：

| 要素 | 类型 | 说明 |
|------|------|------|
| `conventionBuilder` | `MessageConventionBuilder` | 消息分类约定 |
| `strategyBuilders` | `Map<String, TransportStrategyBuilder>` | 按传输名的策略构建器 |
| `registrations` | `CopyOnWriteArrayList<HandlerRegistration>` | 处理器注册列表 |

```java
var configurator = new DefaultConfigurator();

// 配置约定
configurator.setConvention(builder -> builder
    .add(new DefaultMessageConvention())
    .add(new AnnotationMessageConvention()));

// 配置策略
configurator.setStrategy("rabbitmq", builder -> builder
    .add(new AnnotationTransportStrategy()));

// 注册处理器（四种方式）
configurator.registerHandlers(new HandlerRegistration(...));    // 直接注册
configurator.registerHandlers(OrderHandler.class);              // 按类型
configurator.registerHandlers(List.of(OrderHandler.class));     // 按列表
configurator.registerHandlers("com.example.handlers");          // 按包名
```

### MessageHandlerFinder — 处理器发现

自动从类/包中发现消息处理器，支持两种发现路径：

**路径 A：`@Subscribe` 注解**
```java
public class OrderHandler {
    @Subscribe(channel = "orders")
    public void handle(CreateOrderCommand command, MessageContext context) { ... }
}
```

**路径 B：`Handler<M,R>` 接口**
```java
public class OrderHandler implements Handler<CreateOrderCommand, Void> {
    @Override
    public Void handle(CreateOrderCommand command, MessageContext context) { ... }
}
```

发现结果均为 `HandlerRegistration` 记录（channel + messageType + handlerType + method）。

### MessageBusOptions — 运行时选项

| 字段 | 说明 |
|------|------|
| `configurator` | 配置源（最终） |
| `defaultTransport` | 未匹配策略时的默认传输 |
| `enablePipelineBehaviors` | 全局管道行为开关（默认 `true`） |
| `getConvention()` | 从 Configurator 获取组合约定 |
| `getStrategy(name)` | 按名称获取策略 |
| `getStrategyAssignedTypes()` | 返回所有已注册策略名集合 |

### Options 类层次

```
ExtendableOptions (抽象基类)
├── messageId / channel / queue / priority
├── requestTraceId
├── enablePipelineBehaviors
├── attachDefaultPipelineBehaviors
│
├── PublishOptions   (发布专用，无额外字段)
├── SendOptions      (添加 correlationId)
└── CallOptions      (添加 correlationId)
```

### 内部消息类型

| 类 | 说明 |
|----|------|
| `MessageHandler` | 类型擦除的处理器抽象（`Object → Object`），供 `DefaultHandlerContext` 内部使用 |
| `MessageHandlerFactory` | 通过 `ServiceProvider` 创建 `MessageHandler` 的工厂 |
| `PipelineMessage<M,R>` | 绑定消息和管道，支持 `.use()` 追加行为，`.executeAsync()` 触发执行 |
| `MessageCache` | **线程安全单例** — 缓存消息类型 ↔ 通道名映射 |

### MessageCache 命名规则

```
1. 类型标注 @Channel("orders") → 使用 "orders"
2. 未标注                      → 使用完全限定类名 (e.g., "com.example.CreateOrderCommand")
```

---

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.euonia</groupId>
    <artifactId>bus-core</artifactId>
    <version>${revision}</version>
</dependency>
```

### 定义消息与处理器

```java
import com.euonia.bus.contract.Unicast;
import com.euonia.bus.annotation.Subscribe;

// 消息
public class CreateOrderCommand implements Unicast {
    private String productId;
    private int quantity;
    // getters/setters...
}

// 处理器
public class OrderHandler {
    @Subscribe
    public void handle(CreateOrderCommand cmd, MessageContext ctx) {
        // 处理订单创建
        System.out.println("Creating order for: " + cmd.getProductId());
        ctx.response("order-12345");
    }
}
```

### 配置并启动总线

```java
import com.euonia.bus.*;
import com.euonia.bus.convention.*;
import com.euonia.bus.strategy.*;
import com.euonia.bus.options.*;
import com.euonia.reflection.SimpleServiceProvider;

// 1. 准备 ServiceProvider
var provider = new SimpleServiceProvider();
provider.addSingleton(new InMemoryTransport());  // 注册传输实现

// 2. 创建配置
var configurator = new DefaultConfigurator();
configurator.setConvention(b -> b.add(new DefaultMessageConvention()));
configurator.registerHandlers(OrderHandler.class);

// 3. 创建选项
var options = new MessageBusOptions(configurator);
options.setDefaultTransport("inmemory");

// 4. 创建分发器
var dispatcher = new StrategicDispatcher(options);

// 5. 创建总线
var bus = new MessageBus(provider, dispatcher, options);

// 6. 发送消息
bus.sendAsync(new CreateOrderCommand("P123", 5))
    .toCompletableFuture()
    .join();
```

---

## 与其他模块的关系

```
bus-abstract (契约层)
    │
    └──▶ bus-core (本模块 — 编排层)
            │
            ├── 实现 Configurator  → DefaultConfigurator
            ├── 实现 Dispatcher    → StrategicDispatcher
            ├── 实现 HandlerContext → DefaultHandlerContext
            ├── 实现 Bus           → MessageBus
            │
            ├── 依赖 core          → ID 生成、异常、反射
            ├── 依赖 pipeline      → 管道行为（中间件执行）
            │
            └── 消费 Transport (bus-abstract) → 通过 ServiceProvider 解析
                    ├── bus-inmemory  (InMemoryTransport)
                    ├── bus-rabbitmq  (RabbitMqTransport)
                    └── bus-kafka     (待实现)
```

---

## 设计模式

| 模式 | 应用 |
|------|------|
| **策略模式** | `Dispatcher` → `StrategicDispatcher`：可插拔的传输路由策略 |
| **模板方法** | `Bus` 接口：`publishAsync` / `sendAsync` / `callAsync` 默认实现 + 抽象终结点 |
| **外观模式** | `MessageBus` 整合分发、管道、传输三层为统一 API |
| **工厂模式** | `MessageHandlerFactory`：处理器实例的延迟创建 |
| **注册表模式** | `DefaultConfigurator` 聚合约定/策略/注册；`MessageCache` 单例缓存 |
| **观察者模式** | `DefaultHandlerContext` 通过 `SubmissionPublisher` 发布 `MessageSubscribedEvent` |
| **管道模式** | `PipelineMessage` 嵌入 `Pipeline<TRequest,TResponse>`，支持中间件行为链 |
| **选项模式** | `ExtendableOptions` → `PublishOptions` / `SendOptions` / `CallOptions` 层次化选项 |
| **单例模式** | `MessageCache`：双重检查锁定的线程安全单例 |

---

## 许可证

本项目使用 [MIT License](../../LICENSE)。
